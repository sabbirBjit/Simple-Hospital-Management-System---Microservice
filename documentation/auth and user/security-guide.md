# Security Implementation Guide

## Overview
This document outlines the comprehensive security implementation for the Hospital Management System, covering authentication, authorization, data protection, and security best practices.

---

## Authentication System

### JWT (JSON Web Token) Implementation

#### Token Structure
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "iat": 1640995200,
    "exp": 1641081600,
    "userId": 123,
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_PATIENT"]
  },
  "signature": "HMAC_SHA256_SIGNATURE"
}
```

#### Token Configuration
- **Algorithm**: HMAC SHA-256
- **Access Token Expiry**: 24 hours (86400000 ms)
- **Refresh Token Expiry**: 7 days (604800000 ms)
- **Secret Key**: Base64 encoded 256-bit key
- **Issuer**: Hospital Management System

#### Security Features
- ✅ Stateless authentication
- ✅ Token expiration handling
- ✅ Refresh token rotation
- ✅ Secure signature validation
- ✅ Role-based claims

---

## Password Security

### Password Requirements
```regex
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$
```

**Requirements:**
- Minimum 6 characters, maximum 40 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- Special characters recommended but not mandatory

### Password Storage
- **Hashing Algorithm**: BCrypt with salt rounds
- **Salt Rounds**: 12 (configurable)
- **No Plain Text Storage**: Passwords never stored in plain text
- **Hash Verification**: Secure comparison using BCrypt

```java
// Password encoding
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## Authorization & Access Control

### Role-Based Access Control (RBAC)

#### Security Annotations
```java
// Method-level security
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")

// Multiple conditions
@PreAuthorize("hasRole('ADMIN') or (hasRole('DOCTOR') and @securityService.canAccessPatient(#patientId, authentication.principal.id))")
```

#### Permission Matrix
| Operation | Admin | Doctor | Nurse | Patient |
|-----------|-------|--------|-------|---------|
| User CRUD | ✅ | ❌ | ❌ | Own Only |
| Patient Records | ✅ | ✅ | Read Only | Own Only |
| Appointments | ✅ | ✅ | Limited | Own Only |
| System Config | ✅ | ❌ | ❌ | ❌ |

---

## API Gateway Security

### Authentication Filter
```java
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {
    // JWT validation for all incoming requests
    // Skip auth for public endpoints
    // Forward user context to downstream services
}
```

### Security Flow
1. **Request arrives** at API Gateway (port 9000)
2. **Path evaluation** - Check if public endpoint
3. **Token extraction** from Authorization header
4. **Token validation** using JWT utilities
5. **User context** added to headers for downstream services
6. **Request forwarding** to appropriate microservice

### Protected Routes
- All `/api/patients/**` endpoints
- All `/api/appointments/**` endpoints  
- All `/api/notifications/**` endpoints
- User management endpoints in auth service

### Public Routes
- `/api/auth/signin`
- `/api/auth/signup`
- `/api/auth/refresh`
- `/actuator/health`

---

## Data Protection

### Sensitive Data Handling

#### Personal Identifiable Information (PII)
- **Email addresses**: Validated and encrypted in transit
- **Phone numbers**: Format validation and secure storage
- **Medical records**: Restricted access based on roles
- **User credentials**: Secure hashing and salting

#### Database Security
```sql
-- Example security considerations
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt hash
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    failed_login_attempts INT DEFAULT 0,
    last_login TIMESTAMP NULL
);
```

### Encryption Standards
- **Data in Transit**: TLS 1.3 (HTTPS)
- **Data at Rest**: Database-level encryption
- **JWT Tokens**: HMAC SHA-256 signature
- **API Communication**: Encrypted service-to-service communication

---

## Input Validation & Sanitization

### Validation Annotations
```java
public class SignupRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
             message = "Password must contain uppercase, lowercase, and number")
    private String password;
}
```

### Validation Strategy
- **Client-side**: Basic format validation
- **Gateway-level**: Request size and rate limiting
- **Service-level**: Comprehensive business logic validation
- **Database-level**: Constraint enforcement

### Security Validations
- SQL injection prevention through parameterized queries
- XSS prevention through input sanitization
- CSRF protection via stateless JWT tokens
- Request size limiting to prevent DoS attacks

---

## Error Handling & Security

### Secure Error Responses
```json
// Production error response (secure)
{
    "message": "Authentication failed",
    "status": 401,
    "timestamp": "2024-01-15T10:30:00",
    "errors": null
}

// Development error response (detailed)
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "password": "Password must contain uppercase, lowercase, and number"
    }
}
```

### Error Security Principles
- ❌ No sensitive information in error messages
- ❌ No stack traces in production responses
- ❌ No database constraint details exposed
- ✅ Generic error messages for authentication failures
- ✅ Detailed validation errors for user input
- ✅ Proper HTTP status codes

---

## Security Headers & CORS

### CORS Configuration
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: 
              - "http://localhost:3000"  # Frontend
              - "http://localhost:9000"  # Gateway
            allowedMethods:
              - GET
              - POST  
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### Security Headers
```http
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'
```

---

## Logging & Monitoring

### Security Event Logging
```java
// Authentication events
logger.info("User login successful: {}", username);
logger.warn("Failed login attempt for user: {}", username);
logger.error("Invalid JWT token attempt from IP: {}", clientIP);

// Authorization events  
logger.warn("Unauthorized access attempt to {} by user: {}", endpoint, username);
logger.info("Role-based access granted to {} for user: {}", endpoint, username);
```

### Monitored Events
- ✅ Successful and failed login attempts
- ✅ JWT token validation failures
- ✅ Unauthorized access attempts
- ✅ Role escalation attempts
- ✅ Password change events
- ✅ User creation and deletion
- ✅ Suspicious activity patterns

### Log Security
- No sensitive data in logs (passwords, tokens)
- Structured logging for security analysis
- Log rotation and retention policies
- Centralized logging for correlation

---

## Security Testing

### Authentication Testing
```bash
# Test invalid credentials
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "invalid", "password": "wrong"}'

# Test expired token
curl -X GET http://localhost:9000/api/users \
-H "Authorization: Bearer EXPIRED_TOKEN"

# Test role-based access
curl -X DELETE http://localhost:9000/api/users/1 \
-H "Authorization: Bearer PATIENT_TOKEN"
```

### Validation Testing
```bash
# Test input validation
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "a",
    "email": "invalid-email",
    "password": "weak"
}'
```

### Security Test Cases
1. **Authentication Tests**
   - Invalid credentials
   - Expired tokens
   - Malformed JWT tokens
   - Missing authentication headers

2. **Authorization Tests**
   - Role-based access control
   - Resource ownership verification
   - Privilege escalation attempts
   - Cross-user data access

3. **Input Validation Tests**
   - SQL injection attempts
   - XSS payload injection
   - Boundary value testing
   - Malformed request bodies

---

## Security Configuration

### JWT Configuration
```properties
# JWT Security Settings
jwt.secret=BASE64_ENCODED_256_BIT_SECRET
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Security Headers
security.headers.frame-options=DENY
security.headers.content-type-options=nosniff
security.headers.xss-protection=1; mode=block
```

### Database Security
```properties
# Connection Security
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_auth_db?useSSL=true&requireSSL=true
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool Security
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

---

## Security Checklist

### Development Security ✅
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention (output encoding)
- [ ] CSRF protection (stateless design)
- [ ] Secure password storage (BCrypt)
- [ ] JWT token validation
- [ ] Role-based access control
- [ ] Error handling without information leakage

### Deployment Security ✅
- [ ] HTTPS/TLS configuration
- [ ] Environment variable for secrets
- [ ] Database connection encryption
- [ ] Security headers implementation
- [ ] CORS policy configuration
- [ ] Rate limiting implementation
- [ ] Logging and monitoring setup

### Operational Security ✅
- [ ] Regular security audits
- [ ] Dependency vulnerability scanning
- [ ] Access log monitoring
- [ ] Failed authentication tracking
- [ ] Session management
- [ ] Backup security
- [ ] Incident response plan

---

## Security Best Practices

### For Developers
1. **Never hardcode secrets** in source code
2. **Validate all inputs** at multiple layers
3. **Use parameterized queries** to prevent SQL injection
4. **Implement proper error handling** without information disclosure
5. **Follow principle of least privilege** for role assignments
6. **Regular dependency updates** for security patches

### For Administrators  
1. **Regular password policy enforcement**
2. **Monitor authentication logs** for suspicious activity
3. **Implement rate limiting** to prevent brute force attacks
4. **Regular security audits** and penetration testing
5. **Keep systems updated** with latest security patches
6. **Backup security configurations** and test recovery procedures

### For Users
1. **Use strong passwords** meeting system requirements
2. **Don't share credentials** with other users
3. **Report suspicious activity** immediately
4. **Log out properly** after use
5. **Keep personal information updated** and accurate
6. **Understand role limitations** and appropriate access levels

---

## Incident Response

### Security Incident Types
1. **Authentication Bypass**
2. **Unauthorized Data Access**
3. **Privilege Escalation**
4. **Data Breach**
5. **System Compromise**

### Response Procedures
1. **Immediate**: Isolate affected systems
2. **Assessment**: Determine scope and impact
3. **Containment**: Prevent further damage
4. **Recovery**: Restore secure operations
5. **Documentation**: Record incident details
6. **Review**: Improve security measures

### Emergency Contacts
- System Administrator: [Contact Info]
- Security Team: [Contact Info]
- Database Administrator: [Contact Info]
- Network Administrator: [Contact Info]
