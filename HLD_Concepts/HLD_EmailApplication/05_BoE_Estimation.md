# High-Level Design: Email Application - Back-of-the-Envelope (BoE) Estimation (Step 5)

## Overview
Back-of-the-Envelope (BoE) estimation approximates system resources (QPS, storage, bandwidth) to ensure scalability. This is crucial for designing the email application to handle global scale (e.g., like Gmail). We use order-of-magnitude calculations based on realistic assumptions, focusing on key components: user growth, API loads (registration, auth, sending), data storage, and network traffic.

BoE principles:
- Use round numbers and conservative estimates.
- Consider peak loads (e.g., 2x average).
- Ignore constants like π ≈ 3 for simplicity.
- Validate against real systems (e.g., Gmail: ~1.8B users, billions of emails/day).

## Assumptions
- **Global Users**: 1 billion monthly active users (MAU), growing 10% YoY. (Gmail has ~1.8B, so this is large-scale.)
- **Daily Active Users (DAU)**: 20% of MAU = 200M DAU.
- **Emails per Day**: Average 50 emails/DAU (inbox + sent; conservative vs. Gmail's 100+/user). Total: 200M * 50 = 10B emails/day.
- **Peak Load**: 2x average during business hours (e.g., 8 AM - 8 PM UTC, 12-hour peak).
- **User Actions**:
  - Registrations: 1% of DAU new users/day = 2M registrations/day.
  - Logins: 80% of DAU log in daily = 160M logins/day.
  - Sends: Half of emails are sends (other half receives) = 5B sends/day.
  - Reads: 80% of emails read = 8B reads/day.
- **Data Sizes**:
  - User record: 1 KB (username, hash, profile).
  - Email: 10 KB average (body + metadata; attachments extra).
  - Attachment: 100 KB average, 20% of emails have one.
- **API Response Size**: 5 KB average (e.g., email list).
- **Read/Write Ratio**: 80% reads (list/fetch), 20% writes (send/register).
- **Replication**: 3 replicas for DB/queues (high availability).
- **Uptime**: 99.99% (downtime < 5 min/month).

## 1. Throughput Estimation (QPS - Queries Per Second)
QPS measures API/server load. Total seconds/day = 86,400.

### Average QPS
- **Registrations**: 2M / 86,400 ≈ 23 QPS.
- **Logins**: 160M / 86,400 ≈ 1,852 QPS.
- **Sends**: 5B / 86,400 ≈ 57,870 QPS.
- **Reads (e.g., /emails/recent)**: 8B / 86,400 ≈ 92,593 QPS.
- **Total API QPS**: ~23 + 1,852 + 57,870 + 92,593 ≈ 152,338 QPS (average).

### Peak QPS
- Peak factor: 2x during 12 hours (43,200 seconds).
- Peak users: 200M DAU * (12/24) * 2 = 200M (concentrated).
- Peak Sends: 5B * (12/24) * 2 ≈ 5B / 43,200 ≈ 115,740 QPS.
- Peak Reads: 8B * (12/24) * 2 ≈ 185,185 QPS.
- **Total Peak QPS**: ~23*2 + 1,852*2 + 115,740 + 185,185 ≈ 304,658 QPS.

**Explanation**: Divide daily operations by seconds/day for average. For peak, adjust for time window and multiplier. Services like EmailService need to handle ~116K peak QPS for sends; use load balancers with 100-200 servers (assuming 1K QPS/server).

## 2. Storage Estimation
### Users Table
- New users/day: 2M * 1 KB = 2 GB/day.
- Annual: 730M users * 1 KB ≈ 730 GB/year.
- Total (3 years): ~2.2 TB (with growth).
- With indexes/replication (3x): ~20 TB.

### Emails Table (Outgoing + Incoming)
- Emails/day: 10B * 10 KB = 100 TB/day.
- Retention: 1 year (common for email) = 365 * 100 TB ≈ 36.5 PB/year.
- Attachments: 20% * 10B * 100 KB = 200 TB/day → 73 PB/year (store in S3, not DB).
- With compression (2x): Emails ~18.25 PB/year; attachments separate.
- Replication (3x): ~54.75 PB for emails.

**Explanation**: Multiply entities/day * size/entity * retention. PB-scale needs sharding (e.g., by user_id hash) and archival (e.g., Glacier for old emails). Use NoSQL (Cassandra) for emails or relational with partitioning.

### Tokens/Logs
- Tokens: 160M logins * 0.5 KB ≈ 80 GB/day (short-lived, cache in Redis: 1 TB total).
- Logs: 10B events * 1 KB ≈ 10 TB/day → Use ELK with retention 30 days = 300 TB.

**Total Storage**: ~100 PB/year (mostly S3 for attachments/emails). Start with 10 PB provisioned.

## 3. Bandwidth/Network Estimation
### Incoming Traffic (API Requests)
- Requests/day: 13B (sends + reads + logins + etc.) * 1 KB/request ≈ 13 TB/day.
- Peak: 300K QPS * 1 KB * 3600 (hour) * 12 ≈ 13 TB/hour peak.

### Outgoing Traffic (Responses + Delivery)
- Responses: 13B * 5 KB ≈ 65 TB/day.
- Email Delivery: 10B * 10 KB (MIME) ≈ 100 TB/day (to SMTP).
- Attachments Download: 20% * 10B * 100 KB ≈ 200 TB/day (but cached/CDN).
- Total Outgoing: ~365 TB/day.

**Total Bandwidth**: ~378 TB/day (~4.5 GB/s average; peak ~10 GB/s). Use CDN (CloudFront) for attachments; 100 Gbps links.

**Explanation**: Estimate payload sizes per operation. Incoming is smaller (JSON bodies); outgoing dominates due to email content. External delivery adds SMTP bandwidth; monitor with CloudWatch.

## 4. Other Resources
### Compute (Servers)
- API Servers: 300K peak QPS / 1K QPS/server ≈ 300 servers (EmailService/AuthService).
- Queue Processors: 10B events/day / 10K events/sec/processor ≈ 10,000 Kafka consumers.
- DB Shards: 1B users / 1M users/shard ≈ 1,000 shards (PostgreSQL/Cassandra).
- Total: ~5,000 EC2 instances (mix of t3.large for APIs, m5 for DB).

**Cost Estimate** (AWS): Servers ~$500K/month; Storage ~$200K/month (S3); Bandwidth ~$50K/month. Total: ~$1M/month (scale with usage).

### Cache (Redis)
- Hot data: Recent emails (10M active) * 10 KB ≈ 100 GB.
- Sessions/Tokens: 200M * 1 KB ≈ 200 GB.
- Total Cluster: 500 GB (replicated 3x) → 30-node Redis.

## Validation & Edge Cases
- **Bottlenecks**: Queue lag if >1M pending events → Auto-scale consumers.
- **Global Scale**: Add edge locations (CDN) for latency <200ms.
- **Growth**: 10% YoY → Double resources every 7 years (exponential).
- **Compare to Real**: Gmail handles 15B emails/day (~1.5x our 10B); our estimates scale linearly.

## Potential Optimizations
- Caching: Redis for recent reads (hit rate 80% → reduce DB QPS by 4x).
- Compression: Gzip responses (30% savings).
- Sharding: User-based for even load.
- Monitoring: Set alerts at 70% capacity.

## Diagram Reference
(Create `05_BoE_Estimation.png` for resource breakdown: QPS pie chart, storage growth.)

## Next Steps
- Detailed capacity planning with load testing (JMeter).
- Cost optimization (reserved instances, spot fleets).
