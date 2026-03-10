# High-Level Design: Email Application - User Registration (Step 1)

## Overview
User registration is the first step in onboarding for the email application. It involves creating a new user account with a unique username and secure password. The process must handle high traffic, ensure security, validate inputs, and integrate with downstream services like authentication and profile management.

Key goals:
- Validate user input (e.g., unique username, strong password).
- Securely store user credentials.
- Provide immediate feedback (success/error).
- Scale horizontally for millions of users.

Assumptions:
- Username must be unique (e.g., vivek_dalai).
- Password is hashed before storage.
- Optional: Email verification via OTP/SMS (handled in /verify endpoint).
- Integrates with microservices: AuthService for credentials, ProfileService for user details.

## Request Flow
1. **Client Request**: Applications (mobile app, web) send a POST request to the API Gateway.
   ```json
   {
       "endpoint": "/register",
       "method": "POST",
       "body": {
           "username": "vivek_dalai",
           "password": "12345"  // In practice, use stronger passwords and HTTPS
       }
   }
   ```
   - Headers: Include auth tokens if needed, device info for fraud detection.

2. **API Gateway / Reverse Proxy**: Acts as the entry point. Handles:
   - **SSL Termination**: Encrypts traffic (HTTPS).
   - **Rate Limiting**: Prevent DDoS/abuse (e.g., 10 requests/min per IP).
   - **Load Balancing**: Distributes traffic across service instances.
   - **Request Filtering**: Block malicious IPs, validate basic auth.
   - **Routing**: Forwards to appropriate microservice based on endpoint.

3. **Service Discovery & Routing**:
   - Gateway queries a **Service Manager** (e.g., similar to WTSS server or Consul/Eureka) for service details.
   - Service Manager maintains a registry of active services:
     ```json
     [
         {
             "servicename":"AuthService",
             "IPs":["10.10.1.1", "10.10.1.2"],
             "healthCheckAPI": "auth/heath",
             "loadBalancingAlgo": "RoundRobin",
             "handleFor":["/register","/verify","/lastVerified","/sendSMS"]

         },
         {
             "servicename":"ProfileService",
             "IPs":["10.10.2.1", "10.10.2.2","10.10.2.3"],
             "healthCheckAPI": "profile/heath",
             "loadBalancingAlgo": "RoundRobin",
             "handleFor":["/updateProfile","/getProfile"]
         },

     ]
     ```
   - Gateway routes /register to AuthService using load balancing.

## Core Registration Process (AuthService)
1. **Input Validation**:
   - Check username format (alphanumeric, length 3-20).
   - Validate password strength (min 8 chars, mix of letters/numbers/symbols).
   - Query database/cache for username uniqueness.

2. **Database Interaction**:
   - Use a relational DB (e.g., PostgreSQL) for user table:
     ```sql
     CREATE TABLE users (
         id UUID PRIMARY KEY,
         username VARCHAR(50) UNIQUE NOT NULL,
         password_hash VARCHAR(255) NOT NULL,
         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
         is_verified BOOLEAN DEFAULT FALSE
     );
     ```
   - Check: `SELECT username FROM users WHERE username = ?`
     - If exists: Return 409 Conflict ("Username taken").
   - If unique: Hash password (e.g., bcrypt) and insert: `INSERT INTO users (username, password_hash) VALUES (?, ?)`

3. **Security Measures**:
   - Password hashing: Use bcrypt/Argon2 with salt.
   - Rate limiting per username/IP to prevent brute-force.
   - CAPTCHA for suspicious activity.
   - Log attempts for audit (without storing passwords).

4. **Optional Verification**:
   - Generate OTP and send via SMS/Email (integrate with external service like Twilio).
   - Store OTP temporarily (e.g., in Redis with 5-min expiry).
   - Client calls /verify endpoint post-registration.

5. **Response**:
   - Success (201 Created): `{ "message": "Registration successful. Please verify." }`
   - Errors: 400 Bad Request (invalid input), 500 Internal Server Error.

## Integration with ProfileService
- Post-registration, trigger async event (e.g., via Kafka/RabbitMQ) to ProfileService.
- ProfileService creates basic profile: `{ userId, displayName, avatar: default }`.
- Handles /updateProfile later.

## Scalability & Reliability
- **Horizontal Scaling**: Multiple AuthService instances behind load balancer.
- **Caching**: Use Redis for username checks (TTL 1 hour) to reduce DB load.
- **Database Sharding**: Shard by username hash for large scale.
- **Circuit Breaker**: In Gateway to handle service failures (e.g., Hystrix/Resilience4j).
- **Monitoring**: Health checks, metrics (Prometheus), logs (ELK stack).
- **Fault Tolerance**: Retry logic for transient failures, fallback to secondary DB.

## Potential Challenges
- **Race Conditions**: Concurrent registrations for same username → Use DB transactions/locks.
- **High Availability**: Replicate services across regions/AZs.
- **Compliance**: GDPR for user data, secure storage.

## Diagram Reference
See `01_UserRegistration.png` for visual flow (Client → Gateway → AuthService → DB).

## Next Steps
- Implement verification flow (Step 2).
- Handle login post-registration.
