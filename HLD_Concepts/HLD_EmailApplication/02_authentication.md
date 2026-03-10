# High-Level Design: Email Application - Authentication (Step 2)

## Overview
Authentication ensures that only authorized users access protected resources in the email application, such as fetching recent emails (/getRecentEmails). It follows registration and verification, using dynamic tokens to verify identity without exposing credentials in every request. This step focuses on login, token issuance, and request validation.

Key goals:
- Securely authenticate users post-registration.
- Issue short-lived tokens for session management.
- Validate tokens efficiently without DB hits for every request (stateless where possible).
- Handle token expiry, refresh, and revocation.
- Integrate with microservices for seamless access control.

Assumptions:
- Users are registered and verified (from Step 1).
- Use **JWT (JSON Web Tokens)** for stateless auth; alternatives: OAuth2 or session cookies.
- Tokens include claims: user_id, expiry, roles (e.g., user/admin).
- ***Protected endpoints require tokens; public ones (e.g., /register) do not.***

## Authentication Flow
1. **Client Login Request**: After registration/verification, user logs in via POST to API Gateway.
   ```json
   {
       "endpoint": "/login",
       "method": "POST",
       "body": {
           "username": "vivek_dalai",
           "password": "securepass123"  // Hashed on server
       }
   }
   ```
   - Headers: Device info, user-agent for anomaly detection.

2. **API Gateway / Reverse Proxy**: Routes /login to AuthService (via Service Manager, as in Step 1).
   - Applies rate limiting (e.g., 5 attempts/min per username/IP).
   - Forwards request after basic validation.

3. **Login Process (AuthService)**:
   - **Credential Validation**:
     - Retrieve hashed password from DB: `SELECT user_id, password_hash FROM users WHERE username = ?`
     - Compare with provided password using bcrypt (constant-time to prevent timing attacks).
     - If mismatch: Return 401 Unauthorized; increment failed login count (lockout after 5 attempts).
   - **Token Generation** (on success):
     - Create JWT: Signed with private key (HS256/RS256 algorithm).
       ```json
       {
           "header": { "alg": "HS256", "typ": "JWT" },
           "payload": {
               "sub": "user_id",  // e.g., UUID
               "username": "vivek_dalai",
               "iat": 1699123456,  // Issued at (timestamp)
               "exp": 1699127056,  // Expiry (e.g., 1 hour)
               "roles": ["user"]
           },
           "signature": "HMACSHA256(base64UrlEncode(header) + '.' + base64UrlEncode(payload), secret)"
       }
       ```
     - Optional: Issue refresh token (longer-lived, stored securely in DB or HTTP-only cookie).
   - **Response** (200 OK):
     ```json
     {
         "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
         "refresh_token": "refresh_abc123...",  // Optional
         "expires_in": 3600  // Seconds
     }
     ```
     - Client stores token (e.g., localStorage for web, secure storage for mobile).

## Token Validation for Protected Requests
Every subsequent request (e.g., /getRecentEmails) includes the token:
```
/getRecentEmails?username=vivek_dalai&auth_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
Or in Authorization header: `Authorization: Bearer <token>`

1. **API Gateway Validation**:
   - Extract and verify JWT signature using public key/secret.
   - Check expiry (exp > current time) and issued at (iat).
   - If invalid/expired: Reject with 401 Unauthorized.
   - If valid: Route to appropriate service (e.g., EmailService).

2. **Service-Level Validation** (e.g., EmailService):
   - Decode payload to get user_id/username.
   - Optional: Check revocation list (in Redis) or roles for fine-grained access.
   - Proceed with business logic (e.g., fetch emails for user_id).

## Database Interactions
- **Users Table** (from Step 1): For credential checks.
- **Tokens Table** (for refresh/revocation; optional for stateless JWT):
  ```sql
  CREATE TABLE user_tokens (
      id UUID PRIMARY KEY,
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      access_token_hash VARCHAR(255) UNIQUE,  // Hash of token for security
      refresh_token_hash VARCHAR(255) UNIQUE,
      issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      expires_at TIMESTAMP NOT NULL,
      revoked BOOLEAN DEFAULT FALSE,
      device_info VARCHAR(255)  // For multi-device management
  );
  ```
  - On login: Insert new tokens.
  - On logout: Set revoked = TRUE for user_id.
  - Cleanup: Cron job to delete expired tokens.

- **Failed Logins Table** (for security):
  ```sql
  CREATE TABLE failed_logins (
      id UUID PRIMARY KEY,
      username VARCHAR(50),
      ip_address INET,
      attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      user_agent TEXT
  );
  ```

## Security Measures
- **Token Security**: Short expiry (15-60 min for access, 7 days for refresh); use HTTPS only.
- **Refresh Tokens**: Rotate on use; store hashed in DB, send via secure channels.
- **Revocation**: Blacklist compromised tokens in Redis (with TTL); force logout on suspicious activity.
- **Additional Protections**:
  - Multi-Factor Auth (MFA): Integrate TOTP/SMS after login.
  - IP/Device Binding: Validate token against request IP/device.
  - Audit Logs: Track login attempts, token issuances (e.g., in ELK).
  - Prevent CSRF/XSS: Use CORS, validate origins.
- **Common Attacks Mitigated**:
  - Brute-force: Rate limiting, account lockout.
  - Token Theft: Short expiry, secure storage, anomaly detection (e.g., unusual location).

## Scalability & Reliability
- **Stateless JWT**: No DB hit for validation; scales horizontally.
- **Distributed Caching**: Redis cluster for revocation lists/refresh tokens.
- **Key Management**: Rotate signing keys periodically; use asymmetric crypto for services.
- **Load Balancing**: AuthService behind Gateway (RoundRobin, as in Step 1).
- **Fault Tolerance**: Fallback to basic auth if token service down; circuit breakers.
- **Monitoring**: Track login success rate, token expiry errors (Prometheus/Grafana).

## Error Handling
- 401 Unauthorized: Invalid/expired token or credentials.
- 403 Forbidden: Valid token but insufficient roles.
- 429 Too Many Requests: Rate limit exceeded.
- Logging: All failures without sensitive data.

## Integration with Other Services
- Post-auth: Tokens used for EmailService, ProfileService (via Gateway routing).
- Logout: POST /logout → Revoke tokens, clear client storage.
- Session Management: Auto-refresh before expiry.

## Potential Challenges
- **Token Size**: JWT bloat → Use minimal claims; compress if needed.
- **Clock Skew**: Handle NTP sync for expiry checks.
- **Multi-Device**: Limit concurrent sessions; notify on new logins.
- **Compliance**: GDPR for logs; secure key storage (e.g., AWS KMS).

## Diagram Reference
(Reference or create `02_Authentication.png` for flow: Client → Gateway → AuthService → Token Validation → Protected Service.)

## Next Steps
- Implement email fetching with auth (Step 3).
- Add MFA and OAuth integration.
