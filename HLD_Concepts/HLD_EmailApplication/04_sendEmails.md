# High-Level Design: Email Application - Sending Emails (Step 4)

## Overview
Sending emails is a core functionality where authenticated users compose and dispatch messages. This step handles the ingestion of email data, storage, asynchronous processing for delivery, and notifications to related services (e.g., spam detection, tagging). It ensures reliability, scalability for high-volume sends, and eventual consistency across microservices.

Key goals:
- Validate and store outgoing emails securely.
- Decouple sending from delivery using events for fault tolerance.
- Integrate spam/preference checks to prevent abuse.
- Support attachments, rich content, and multi-recipient sends.
- Scale to handle millions of emails/day.

Assumptions:
- Users are authenticated (JWT from Step 2).
- Emails use API contracts (e.g., POST /v1/emails/send from Step 3).
- Attachments stored in object storage (e.g., S3); links in email body.
- Delivery to external providers (e.g., SMTP relays like SendGrid) is async.
- Eventual consistency via message queues; no immediate delivery guarantees.

## Request Flow
1. **Client Request**: Authenticated user sends email via API Gateway.
   - Endpoint: POST /v1/emails/send (from Step 3).
   - Headers: Authorization: Bearer <JWT>.
   - Request Body (JSON):
     ```json
     {
         "to": ["recipient1@example.com", "recipient2@example.com"],  // Array for multi-recipient
         "cc": ["cc@example.com"],  // Optional
         "bcc": ["bcc@example.com"],  // Optional, hidden from recipients
         "subject": "string",  // Max 78 chars (RFC 5322)
         "body": "string (HTML/text)",  // Supports MIME types
         "attachments": [  // Optional
             {
                 "filename": "string",
                 "contentType": "application/pdf",
                 "data": "base64-encoded"  // Or URL for large files
             }
         ],
         "priority": "low|normal|high"  // Optional
     }
     ```
   - Validation: AuthService extracts user_id from JWT; check send limits (e.g., 100/day).

2. **API Gateway / Reverse Proxy**: Routes to EmailService (via Service Manager/Contract Registry, Steps 1 & 3).
   - Rate limiting: Per-user quotas (e.g., 500 emails/hour).
   - Injects X-User-ID header from token.
   - Forwards after auth validation.

3. **EmailService Processing**:
   - **Ingestion & Validation**:
     - Parse body; scan for malware/viruses (integrate ClamAV).
     - Check recipient validity (syntax, domain MX records).
     - Enforce policies: Attachment size < 25MB, no spam words.
     - Generate unique messageId (UUID).
   - **Storage**:
     - Store in relational DB (e.g., PostgreSQL) for outbox pattern:
       ```sql
       CREATE TABLE outgoing_emails (
           id UUID PRIMARY KEY,
           message_id UUID UNIQUE NOT NULL,
           sender_id UUID REFERENCES users(id),
           to_emails TEXT[] NOT NULL,  // Array of recipients
           cc_emails TEXT[],  // Optional
           bcc_emails TEXT[],  // Optional
           subject VARCHAR(255) NOT NULL,
           body TEXT NOT NULL,  // JSON: { "text": "...", "html": "...", "attachments": [...] }
           attachments JSONB,  // { "filename": "...", "s3_url": "..." }
           status VARCHAR(20) DEFAULT 'pending',  // pending, sent, failed, delivered
           priority VARCHAR(10) DEFAULT 'normal',
           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
           sent_at TIMESTAMP,
           error_details TEXT  // For failures
       );
       -- Indexes: sender_id, created_at for queries; message_id UNIQUE
       ```
     - Upload attachments to S3; store URLs in body JSON.
     - Insert record: `INSERT INTO outgoing_emails (message_id, sender_id, to_emails, ..., body) VALUES (?, ?, ?, ...);`
   - **Response** (202 Accepted, async):
     ```json
     {
         "messageId": "uuid",
         "status": "accepted",
         "message": "Email queued for delivery"
     }
     ```
     - Errors: 400 (Invalid recipients/body), 429 (Quota exceeded), 413 (Payload too large).

4. **Event Publishing**:
   - On successful storage, publish event to Message Queue (e.g., Kafka topic: "email-sent-events").
     - Event Schema (Avro/JSON):
       ```json
       {
           "eventType": "email_created",
           "messageId": "uuid",
           "senderId": "uuid",
           "to": ["string"],
           "subject": "string",
           "timestamp": "ISO 8601",
           "priority": "string"
       }
       ```
   - Ensures at-least-once delivery; idempotency via messageId.

## Asynchronous Processing & Subscribers
EmailService acts as publisher; other services subscribe for loose coupling and eventual consistency.

1. **Message Queue Setup**:
   - Use Kafka (partitioned by sender_id for ordering) or RabbitMQ for queues.
   - Topics: "email-sent-events" (for internal processing), "delivery-events" (for external SMTP).

2. **Subscriber Services**:
   - **SpamDetectionService**:
     - Consumes events; analyzes content (e.g., using ML models like SpamAssassin).
     - Flags spam: Update status to 'spam_detected'; notify sender or quarantine.
     - Threshold: Score > 5/10 → Block send.
   - **TaggingService**:
     - Applies tags/labels (e.g., auto-categorize as "work" via NLP).
     - Stores in separate table: `email_tags (message_id, tags: ["urgent", "invoice"])`.
   - **PreferenceService**:
     - Checks recipient preferences (e.g., unsubscribe status, time zones).
     - Integrates with ProfileService (Step 3); suppresses sends if opted out.
   - **DeliveryService** (new microservice):
     - Consumes "email-sent-events"; formats MIME message.
     - Routes to external SMTP (e.g., AWS SES, SendGrid) for actual delivery.
     - Handles bounces/retries (exponential backoff, dead-letter queue).
     - Updates status: 'sent' on dispatch, 'delivered' on ACK, 'failed' on error.
   - **NotificationService**:
     - Sends push/email notifications to recipients (e.g., "New email from vivek_dalai").

3. **Eventual Consistency**:
   - Services process independently; use DB transactions for critical ops.
   - Reconciliation: Periodic jobs sync statuses (e.g., if delivery fails post-send).
   - Idempotency: Consumers check if messageId already processed (via Redis cache).

## Scalability & Reliability
- **Horizontal Scaling**: EmailService instances load-balanced (RoundRobin); Kafka partitions scale with load.
- **Database**: Shard outgoing_emails by sender_id; read replicas for queries.
- **Queue Durability**: Kafka replication (3x); dead-letter queues for failures.
- **Throttling**: Per-sender queues to prevent overload.
- **Monitoring**: Track queue lag, delivery rates (Prometheus); alert on high failure rates.
- **Fault Tolerance**: Circuit breakers for external SMTP; fallback to retry queues.
- **High Availability**: Multi-AZ deployment; geo-replication for global users.

## Security Measures
- **Auth & Authorization**: JWT validation at Gateway; sender_id must match token.
- **Content Scanning**: Virus/spam checks before queuing.
- **Encryption**: Body/attachments encrypted at rest (DB encryption); in-transit (TLS).
- **Compliance**: DKIM/SPF for outgoing; GDPR for recipient data.
- **Abuse Prevention**: Rate limits, CAPTCHA for bulk sends.

## Error Handling
- **Sync Errors**: HTTP 4xx/5xx as per API contracts (Step 3).
- **Async Errors**: Update DB status; publish failure events (e.g., "email_delivery_failed").
- **Retries**: 3 attempts for transient failures (e.g., SMTP timeout).
- **Global Format** (Step 3): `{ "error": { "code": "EMAIL_SEND_FAILED", "message": "..." } }`.

## Integration with Prior Steps
- **Auth (Step 2)**: JWT for sender verification.
- **API Contracts (Step 3)**: Uses defined /v1/emails/send; events follow schema.
- **Registration (Step 1)**: Sender must be verified user.

## Potential Challenges
- **High Volume**: Queue backlogs → Use auto-scaling, priority queues.
- **Delivery Guarantees**: External SMTP failures → Implement tracking pixels for opens.
- **Consistency Delays**: Spam detection post-send → Allow recalls for recent emails.
- **Attachments**: Large files → Async upload to S3 before queuing.
- **Internationalization**: Handle UTF-8, timezones in timestamps.

## Diagram Reference
See `04_send_emails.png` for visual flow (Client → Gateway → EmailService → MQ → Subscribers → Delivery).

## Next Steps
- Design email storage/retrieval (inbox, search) (Step 5).
- Implement real-time features (e.g., typing indicators via WebSockets).
