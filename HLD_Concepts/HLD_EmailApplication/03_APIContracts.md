# High-Level Design: Email Application - API Contracts (Step 3)

## Overview
API contracts define the interfaces between clients and services, and among microservices, ensuring consistency, discoverability, and maintainability. In a distributed email application, contracts prevent breaking changes, enable automated testing (e.g., contract testing with Pact), and facilitate documentation. They include endpoints, methods, request/response schemas, authentication, and error formats.

Key principles:
- **RESTful Design**: Use HTTP methods (GET/POST/PUT/DELETE), meaningful paths, status codes.
- **Versioning**: Prefix paths (e.g., /v1/register) to evolve APIs without disruption.
- **Documentation**: Use OpenAPI (Swagger) specs for auto-generated docs/UI.
- **Consistency**: Standardize headers (e.g., Content-Type: application/json), errors, pagination.
- **Security**: Mandate auth for protected endpoints (JWT from Step 2).

Assumptions:
- All APIs routed via API Gateway (Step 1).
- Synchronous calls for user-facing APIs; async (e.g., Kafka) for inter-service events.
- JSON payloads; support for pagination (offset/limit) in list endpoints.

## Contract Registry Service
A centralized **Contract Registry** (e.g., implemented with Spring Cloud Contract or a simple DB/service like Consul with OpenAPI YAML files) manages API definitions:
- **Purpose**: Stores, versions, and distributes contracts to services/Gateway. Services register their APIs on startup; clients query for latest specs.
- **Features**:
  - **Discovery**: Gateway queries registry for endpoint routing (extends Service Manager from Step 1).
  - **Validation**: Runtime checks (e.g., schema validation with JSON Schema).
  - **Version Control**: Track changes, deprecate old versions.
  - **Storage**: DB table for metadata + Git/S3 for spec files.
    ```sql
    CREATE TABLE api_contracts (
        id UUID PRIMARY KEY,
        service_name VARCHAR(50) NOT NULL,
        version VARCHAR(10) NOT NULL,  -- e.g., "v1"
        endpoint_path VARCHAR(255) NOT NULL,
        method VARCHAR(10) NOT NULL,  -- GET, POST, etc.
        spec_file_path VARCHAR(255),  -- Path to OpenAPI YAML
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        deprecated BOOLEAN DEFAULT FALSE
    );
    ```
- **Workflow**:
  1. Service deploys with OpenAPI spec → Registers in Registry.
  2. Gateway pulls specs for validation/routing.
  3. Clients (apps) fetch specs for codegen (e.g., Swagger Codegen).
- **Tools**: OpenAPI 3.0 for specs; API Gateway (Kong/AWS API Gateway) for enforcement.

Example Registry Entry (JSON representation):
```json
{
    "serviceName": "AuthService",
    "version": "v1",
    "endpoints": [
        {
            "path": "/register",
            "method": "POST",
            "authRequired": false,
            "description": "Register new user"
        }
    ]
}
```

## Core API Endpoints
Grouped by service. All responses include standard headers: X-Request-ID, X-Rate-Limit-Remaining.

### AuthService APIs (v1)
- **POST /v1/register**
  - Description: Register a new user (Step 1).
  - Auth: None.
  - Request Body:
    ```json
    {
        "username": "string",  // Unique, 3-20 chars
        "password": "string"   // Min 8 chars, strong policy
    }
    ```
  - Response (201 Created):
    ```json
    {
        "message": "Registration successful. Please verify.",
        "userId": "uuid"
    }
    ```
  - Errors: 400 (Invalid input), 409 (Username taken).

- **POST /v1/login**
  - Description: Authenticate and get tokens (Step 2).
  - Auth: None.
  - Request Body:
    ```json
    {
        "username": "string",
        "password": "string"
    }
    ```
  - Response (200 OK):
    ```json
    {
        "access_token": "string (JWT)",
        "refresh_token": "string",
        "expires_in": 3600
    }
    ```
  - Errors: 401 (Invalid credentials), 429 (Too many attempts).

- **POST /v1/verify**
  - Description: Verify OTP post-registration.
  - Auth: None.
  - Request Body:
    ```json
    {
        "username": "string",
        "otp": "string (6 digits)"
    }
    ```
  - Response (200 OK): `{ "message": "Verified successfully" }`

- **POST /v1/logout**
  - Description: Revoke tokens.
  - Auth: Bearer JWT.
  - Request Body: Empty.
  - Response (200 OK): `{ "message": "Logged out" }`

- **POST /v1/refresh**
  - Description: Get new access token.
  - Auth: Refresh token in body.
  - Request Body: `{ "refresh_token": "string" }`
  - Response (200 OK): New access_token.

### EmailService APIs (v1)
- **GET /v1/emails/recent**
  - Description: Fetch recent emails (from Step 2 example).
  - Auth: Bearer JWT.
  - Query Params:
    - `limit`: integer (default 20, max 100)
    - `offset`: integer (default 0)
  - Response (200 OK):
    ```json
    {
        "emails": [
            {
                "id": "uuid",
                "subject": "string",
                "from": "string",
                "to": "string",
                "body": "string",
                "timestamp": "ISO 8601",
                "read": boolean
            }
        ],
        "total": integer,
        "nextOffset": integer
    }
    ```
  - Errors: 401 (Unauthorized).

- **POST /v1/emails/send**
  - Description: Send an email.
  - Auth: Bearer JWT.
  - Request Body:
    ```json
    {
        "to": ["string"],  // Recipients
        "subject": "string",
        "body": "string",
        "cc": ["string"],  // Optional
        "bcc": ["string"]  // Optional
    }
    ```
  - Response (202 Accepted): `{ "messageId": "uuid" }`  // Async processing.

- **GET /v1/emails/{emailId}**
  - Description: Fetch single email.
  - Auth: Bearer JWT.
  - Path Param: emailId (uuid).
  - Response (200 OK): Single email object.

- **PUT /v1/emails/{emailId}/read**
  - Description: Mark as read.
  - Auth: Bearer JWT.
  - Response (200 OK): Updated email.

### ProfileService APIs (v1)
- **GET /v1/profile**
  - Description: Get user profile.
  - Auth: Bearer JWT.
  - Response (200 OK):
    ```json
    {
        "userId": "uuid",
        "username": "string",
        "displayName": "string",
        "avatar": "string (URL)",
        "preferences": { "theme": "dark" }
    }
    ```

- **PUT /v1/profile**
  - Description: Update profile.
  - Auth: Bearer JWT.
  - Request Body: Partial profile object.
  - Response (200 OK): Updated profile.

## Inter-Service Communication
- **Synchronous**: REST/ gRPC between services (e.g., AuthService calls ProfileService post-register).
- **Asynchronous**: Events via message broker (e.g., Kafka topics: userRegistered → ProfileService creates profile).
- **Contracts for Internal APIs**: Similar OpenAPI specs, but private (not exposed to clients).

## Error Handling & Standards
- **Global Error Format**:
  ```json
  {
      "error": {
          "code": "string",  // e.g., "AUTH_INVALID"
          "message": "string",
          "details": {}  // Optional
      }
  }
  ```
- **HTTP Status Codes**:
  - 2xx: Success.
  - 4xx: Client errors (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 429 Rate Limit).
  - 5xx: Server errors (500 Internal, 503 Service Unavailable).
- **Headers**:
  - Authorization: Bearer <token>
  - X-User-ID: Injected by Gateway from token.
  - Rate-Limit: Remaining requests.

## Versioning & Evolution
- **Path Versioning**: /v1/emails → /v2/emails (add new fields, deprecate old).
- **Backward Compatibility**: Never remove fields; use nullable or optional.
- **Deprecation**: Announce in docs; sunset after 6 months.
- **Contract Testing**: Automated tests ensure producer/consumer compatibility.

### Examples
#### Example 1: Evolving /v1/emails/recent to /v2/emails/recent
- **v1 Response** (Basic):
  ```json
  {
      "emails": [
          {
              "id": "uuid",
              "subject": "string",
              "from": "string",
              "timestamp": "ISO 8601"
          }
      ],
      "total": integer
  }
  ```
- **v2 Response** (Enhanced: Add 'priority' and 'attachments'; deprecate 'total' in favor of 'metadata'):
  ```json
  {
      "emails": [
          {
              "id": "uuid",
              "subject": "string",
              "from": "string",
              "priority": "low|medium|high",  // New field
              "attachments": [{"name": "string", "size": integer}],  // New array
              "timestamp": "ISO 8601"
          }
      ],
      "metadata": {  // Replaces 'total'
          "total": integer,
          "unread": integer  // New
      }
  }
  ```
  - Clients using v1 continue unchanged; v2 clients opt-in.
  - Deprecate v1 after announcing in changelog/docs.

#### Example 2: OpenAPI Spec Snippet for Versioning
In OpenAPI YAML (stored in Contract Registry):
```yaml
paths:
  /v1/emails/recent:
    get:
      summary: Get recent emails (v1)
      deprecated: true  # Mark for sunset
      responses:
        '200':
          content:
            application/json:
              schema:
                type: object
                properties:
                  emails:
                    type: array
                    items:
                      type: object
                      properties:
                        id: { type: string }
                        subject: { type: string }
                        from: { type: string }
                        timestamp: { type: string }
                  total: { type: integer }

  /v2/emails/recent:
    get:
      summary: Get recent emails (v2)
      responses:
        '200':
          content:
            application/json:
              schema:
                type: object
                properties:
                  emails:
                    type: array
                    items:
                      type: object
                      properties:
                        id: { type: string }
                        subject: { type: string }
                        from: { type: string }
                        priority: { type: string, enum: [low, medium, high] }
                        attachments: { type: array, items: { type: object } }
                        timestamp: { type: string }
                  metadata:
                    type: object
                    properties:
                      total: { type: integer }
                      unread: { type: integer }
```

#### Example 3: Contract Testing (Pact)
Producer (EmailService) and Consumer (Client App) define pacts:
- Pact verifies v1 response matches contract.
- On v2 release, update pact and re-test interactions.
- CI/CD: Fail build if contracts break.

#### Example 4: Header-Based Versioning (Stripe Style)
- **Approach**: Some platforms (e.g., Stripe) use request-based versioning via a custom header (e.g., Api-Version or Stripe-Version) instead of changing URLs. This keeps endpoints stable while allowing behavioral differences based on the requested version.

- **Example Requests**:
  - Older client:
    ```
    POST /customers
    Stripe-Version: 2020-08-27
    Body: { "name": "John Doe", "email": "john@example.com" }
    ```
    Response: Old format (e.g., without new fields like "tax_id").

  - Newer client:
    ```
    POST /customers
    Stripe-Version: 2022-11-15
    Body: { "name": "John Doe", "email": "john@example.com", "tax_id": "123-45-6789" }
    ```
    Response: New format (includes "tax_id" support, additional metadata).

- **Server Logic**:
  - Extract version from header.
  - If version == "2020-08-27": Use old response schema/logic (ignore new fields).
  - If version == "2022-11-15": Use new schema/logic (validate/enforce new fields).
  - Default: Latest version if unspecified.
  - Pros: URLs don't change (better for caching/SEO); easier client migration.
  - Cons: Requires header inspection in Gateway/services; potential for version-specific bugs.

- **Implementation in Our System**:
  - Gateway checks Api-Version header and routes to versioned handlers in services.
  - Contract Registry: Map header values to spec versions (e.g., "2020-08-27" → v1 OpenAPI spec).
  - Deprecation: Return warnings in headers (e.g., Deprecation: true) for old versions; sunset after announcement.

## Monitoring & Documentation
- **OpenAPI Specs**: Generate interactive docs at /api-docs (Swagger UI).
- **Metrics**: Track API usage, latency (Prometheus).
- **Tools**: Postman/Newman for testing; Spectral for linting specs.

## Potential Challenges
- **Contract Drift**: Services evolve independently → Use CI/CD gates for validation.
- **Performance**: Schema validation overhead → Cache specs.
- **Multi-Version Support**: Resource-intensive → Phase out old versions gradually.

## Diagram Reference
(Reference or create `03_APIContracts.png` for endpoint overview and registry flow.)

## Next Steps
- Define detailed schemas for email storage/retrieval (Step 4).
- Implement API Gateway enforcement of contracts.
