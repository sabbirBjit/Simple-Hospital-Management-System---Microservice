# Authentication Service API Documentation

## Base URL
```
http://localhost:8081
```

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /api/auth/signup`

**Description:** Register a new user in the system

**Request Body:**
```json
{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["patient"]
}
```

**Validation Rules:**
- `username`: Required, 3-20 characters, alphanumeric and underscores only
- `email`: Required, valid email format, max 50 characters
- `password`: Required, 6-40 characters, must contain uppercase, lowercase, and number
- `firstName`: Required, 2-50 characters, letters and spaces only
- `lastName`: Required, 2-50 characters, letters and spaces only
- `phoneNumber`: Optional, 10-15 characters, valid phone format
- `roles`: Optional array, defaults to ["patient"]

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User registered successfully!",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**Response (Validation Error - 400):**
```json
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "username": "Username must be between 3 and 20 characters",
        "email": "Please provide a valid email address",
        "password": "Password must contain at least one uppercase letter, one lowercase letter, and one number",
        "firstName": "First name is required"
    }
}
```

**Response (Business Logic Error - 400):**
```json
{
    "success": false,
    "message": "Username is already taken!",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["patient"]
}'
```

---

### 2. Patient Registration (Simplified)
**Endpoint:** `POST /api/auth/signup/patient`

**Description:** Simplified patient registration endpoint that automatically assigns patient role

**Request Body:**
```json
{
    "username": "patient_smith",
    "email": "patient.smith@example.com",
    "password": "Password123",
    "firstName": "Sarah",
    "lastName": "Smith",
    "phoneNumber": "+1234567890"
}
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patient registered successfully! A patient profile has been created for you.",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/signup/patient \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_smith",
    "email": "patient.smith@example.com",
    "password": "Password123",
    "firstName": "Sarah",
    "lastName": "Smith",
    "phoneNumber": "+1234567890"
}'
```

---

### 3. User Login
**Endpoint:** `POST /api/auth/signin`

**Description:** Authenticate user and receive JWT tokens

**Request Body:**
```json
{
    "username": "john_doe",
    "password": "Password123"
}
```

**Validation Rules:**
- `username`: Required, 3-50 characters
- `password`: Required, max 40 characters

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Login successful",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "type": "Bearer",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "roles": ["ROLE_PATIENT"]
    }
}
```

**Response (Authentication Error - 401):**
```json
{
    "message": "Invalid username or password",
    "status": 401,
    "timestamp": "2024-01-15T10:30:00",
    "errors": null
}
```

**Response (Validation Error - 400):**
```json
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "username": "Username is required",
        "password": "Password is required"
    }
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "password": "Password123"
}'
```

---

### 4. Refresh Token
**Endpoint:** `POST /api/auth/refresh`

**Description:** Get new access token using refresh token

**Request Body:**
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Token refreshed successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "type": "Bearer",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "roles": ["ROLE_PATIENT"]
    }
}
```

**Response (Error - 400):**
```json
{
    "success": false,
    "message": "Token refresh failed: Invalid or expired refresh token",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
-H "Content-Type: application/json" \
-d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
}'
```

---

### 5. Validate Token
**Endpoint:** `POST /api/auth/validate`

**Description:** Validate if a JWT token is valid

**Request Body:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Token is valid",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "valid": true
    }
}
```

**Response (Invalid Token - 200):**
```json
{
    "success": true,
    "message": "Token is invalid",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "valid": false
    }
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/validate \
-H "Content-Type: application/json" \
-d '{
    "token": "YOUR_JWT_TOKEN_HERE"
}'
```

---

### 6. User Logout
**Endpoint:** `POST /api/auth/logout`

**Description:** Logout user (client-side token removal)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User logged out successfully!",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/logout
```

---

## User Management Endpoints

### 7. Get All Users
**Endpoint:** `GET /api/users`

**Description:** Get list of all users (Admin, Doctor, Nurse only)

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Users retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "username": "john_doe",
            "email": "john.doe@example.com",
            "firstName": "John",
            "lastName": "Doe",
            "phoneNumber": "+1234567890",
            "isActive": true,
            "isEmailVerified": false,
            "createdAt": "2023-01-01T10:00:00",
            "roles": ["ROLE_PATIENT"]
        },
        {
            "id": 2,
            "username": "dr_smith",
            "email": "dr.smith@hospital.com",
            "firstName": "Dr. Jane",
            "lastName": "Smith",
            "phoneNumber": "+1234567891",
            "isActive": true,
            "isEmailVerified": true,
            "createdAt": "2023-01-01T09:00:00",
            "roles": ["ROLE_DOCTOR"]
        }
    ]
}
```

**Response (Unauthorized - 403):**
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "Access Denied",
    "path": "/api/users"
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/users \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

### 8. Get User by ID
**Endpoint:** `GET /api/users/{id}`

**Description:** Get specific user by ID

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User found",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "phoneNumber": "+1234567890",
        "isActive": true,
        "isEmailVerified": false,
        "createdAt": "2023-01-01T10:00:00",
        "roles": ["ROLE_PATIENT"]
    }
}
```

**Response (Not Found - 404):**
```
HTTP 404 Not Found
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/users/1 \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

### 9. Get User by Username
**Endpoint:** `GET /api/users/username/{username}`

**Description:** Get specific user by username

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User found",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "phoneNumber": "+1234567890",
        "isActive": true,
        "isEmailVerified": false,
        "createdAt": "2023-01-01T10:00:00",
        "roles": ["ROLE_PATIENT"]
    }
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/users/username/john_doe \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

### 10. Get Users by Role
**Endpoint:** `GET /api/users/role/{roleName}`

**Description:** Get users by specific role (Admin only)

**Required Roles:** ROLE_ADMIN

**Path Parameters:**
- `roleName`: admin, doctor, nurse, patient

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Users with role doctor retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 2,
            "username": "dr_smith",
            "email": "dr.smith@hospital.com",
            "firstName": "Dr. Jane",
            "lastName": "Smith",
            "phoneNumber": "+1234567891",
            "isActive": true,
            "isEmailVerified": true,
            "createdAt": "2023-01-01T09:00:00",
            "roles": ["ROLE_DOCTOR"]
        },
        {
            "id": 3,
            "username": "dr_johnson",
            "email": "dr.johnson@hospital.com",
            "firstName": "Dr. Michael",
            "lastName": "Johnson",
            "phoneNumber": "+1234567892",
            "isActive": true,
            "isEmailVerified": true,
            "createdAt": "2023-01-02T09:00:00",
            "roles": ["ROLE_DOCTOR"]
        }
    ]
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/users/role/doctor \
-H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN_HERE"
```

---

### 11. Update User
**Endpoint:** `PUT /api/users/{id}`

**Description:** Update user profile

**Required Roles:** ROLE_ADMIN or own profile

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:**
```json
{
    "firstName": "John Updated",
    "lastName": "Doe Updated",
    "email": "john.updated@example.com",
    "phoneNumber": "+1234567899",
    "isActive": true
}
```

**Validation Rules:**
- `firstName`: Optional, 2-50 characters, letters and spaces only
- `lastName`: Optional, 2-50 characters, letters and spaces only
- `email`: Optional, valid email format, max 50 characters, must be unique
- `phoneNumber`: Optional, 10-15 characters, valid phone format
- `isActive`: Optional boolean

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User updated successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "username": "john_doe",
        "email": "john.updated@example.com",
        "firstName": "John Updated",
        "lastName": "Doe Updated",
        "phoneNumber": "+1234567899",
        "isActive": true,
        "isEmailVerified": false,
        "createdAt": "2023-01-01T10:00:00",
        "updatedAt": "2023-01-02T10:00:00"
    }
}
```

**Response (Email Already Exists - 400):**
```json
{
    "success": false,
    "message": "Failed to update user: Email is already in use by another user",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X PUT http://localhost:8081/api/users/1 \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
-H "Content-Type: application/json" \
-d '{
    "firstName": "John Updated",
    "lastName": "Doe Updated",
    "email": "john.updated@example.com",
    "phoneNumber": "+1234567899",
    "isActive": true
}'
```

---

### 12. Delete User
**Endpoint:** `DELETE /api/users/{id}`

**Description:** Delete user (Admin only)

**Required Roles:** ROLE_ADMIN

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User deleted successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**Response (User Not Found - 400):**
```json
{
    "success": false,
    "message": "Failed to delete user: User not found with id: 999",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

**cURL Command:**
```bash
curl -X DELETE http://localhost:8081/api/users/1 \
-H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN_HERE"
```

---

## API Gateway Integration

### Base URL via Gateway
```
http://localhost:9000
```

All authentication endpoints are accessible through the API Gateway:
- `POST http://localhost:9000/api/auth/signin`
- `POST http://localhost:9000/api/auth/signup`
- `POST http://localhost:9000/api/auth/signup/patient`
- `POST http://localhost:9000/api/auth/refresh`
- `POST http://localhost:9000/api/auth/validate`
- `POST http://localhost:9000/api/auth/logout`

**Gateway Authentication Flow:**
1. Login via gateway to get JWT token
2. Include `Authorization: Bearer <token>` header for protected routes
3. Gateway validates token before forwarding to services
4. Invalid tokens return 401 Unauthorized
5. User context is forwarded to downstream services via headers

**Gateway Headers Added:**
```http
X-User-Name: john_doe
X-User-Id: 1
```

---

## Health Check Endpoints

### 13. Health Check
**Endpoint:** `GET /actuator/health`

**Description:** Service health status

**Response (Success - 200):**
```json
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "MySQL",
                "validationQuery": "isValid()"
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 499963174912,
                "free": 91943546880,
                "threshold": 10485760,
                "exists": true
            }
        }
    }
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/actuator/health
```

---

### 14. Application Info
**Endpoint:** `GET /actuator/info`

**Description:** Application information

**Response (Success - 200):**
```json
{
    "app": {
        "name": "auth-service",
        "description": "Authentication and user management",
        "version": "1.0.0"
    },
    "java": {
        "version": "21.0.1",
        "vendor": {
            "name": "Eclipse Adoptium"
        }
    }
}
```

---

## Complete Sample Test Data

### Create System Users
```bash
# Create Admin User
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "admin",
    "email": "admin@hospital.com",
    "password": "Admin123",
    "firstName": "System",
    "lastName": "Administrator",
    "phoneNumber": "+1234567890",
    "roles": ["admin"]
}'

# Create Head Doctor
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_chief",
    "email": "chief.doctor@hospital.com",
    "password": "Doctor123",
    "firstName": "Dr. Robert",
    "lastName": "Wilson",
    "phoneNumber": "+1234567891",
    "roles": ["doctor", "admin"]
}'

# Create Multiple Doctors
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_smith",
    "email": "dr.smith@hospital.com",
    "password": "Doctor123",
    "firstName": "Dr. Jane",
    "lastName": "Smith",
    "phoneNumber": "+1234567892",
    "roles": ["doctor"]
}'

curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_johnson",
    "email": "dr.johnson@hospital.com",
    "password": "Doctor123",
    "firstName": "Dr. Michael",
    "lastName": "Johnson",
    "phoneNumber": "+1234567893",
    "roles": ["doctor"]
}'

# Create Multiple Nurses
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "nurse_mary",
    "email": "mary.johnson@hospital.com",
    "password": "Nurse123",
    "firstName": "Mary",
    "lastName": "Johnson",
    "phoneNumber": "+1234567894",
    "roles": ["nurse"]
}'

curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "nurse_sarah",
    "email": "sarah.williams@hospital.com",
    "password": "Nurse123",
    "firstName": "Sarah",
    "lastName": "Williams",
    "phoneNumber": "+1234567895",
    "roles": ["nurse"]
}'

# Create Multiple Patients
curl -X POST http://localhost:9000/api/auth/signup/patient \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_john",
    "email": "john.doe@example.com",
    "password": "Patient123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567896"
}'

curl -X POST http://localhost:9000/api/auth/signup/patient \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_alice",
    "email": "alice.brown@example.com",
    "password": "Patient123",
    "firstName": "Alice",
    "lastName": "Brown",
    "phoneNumber": "+1234567897"
}'

curl -X POST http://localhost:9000/api/auth/signup/patient \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_bob",
    "email": "bob.wilson@example.com",
    "password": "Patient123",
    "firstName": "Bob",
    "lastName": "Wilson",
    "phoneNumber": "+1234567898"
}'
```

---

## Complete Integration Testing

### Authentication Flow Test
```bash
#!/bin/bash

# 1. Login to get token
echo "Testing login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "admin",
    "password": "Admin123"
}')

echo "Login Response: $LOGIN_RESPONSE"

# Extract token from response (requires jq)
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
echo "Extracted Token: $TOKEN"

# 2. Test protected endpoint with token
echo "Testing protected endpoint..."
curl -X GET http://localhost:9000/api/users \
-H "Authorization: Bearer $TOKEN"

# 3. Test token validation
echo "Testing token validation..."
curl -X POST http://localhost:9000/api/auth/validate \
-H "Content-Type: application/json" \
-d "{\"token\": \"$TOKEN\"}"

# 4. Test role-based access
echo "Testing role-based access..."
curl -X GET http://localhost:9000/api/users/role/doctor \
-H "Authorization: Bearer $TOKEN"
```

### Authorization Test Script
```bash
#!/bin/bash

# Create patient user and get token
PATIENT_LOGIN=$(curl -s -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_john",
    "password": "Patient123"
}')

PATIENT_TOKEN=$(echo $PATIENT_LOGIN | jq -r '.data.token')

# Test patient trying to access admin endpoint (should fail)
echo "Testing unauthorized access..."
curl -X GET http://localhost:9000/api/users/role/admin \
-H "Authorization: Bearer $PATIENT_TOKEN"

# Test patient accessing own data (should work if implemented)
echo "Testing authorized access..."
curl -X GET http://localhost:9000/api/users/1 \
-H "Authorization: Bearer $PATIENT_TOKEN"
```

---

## Validation Rules Summary

### Username Validation
- **Required**: Yes
- **Length**: 3-20 characters
- **Pattern**: `^[a-zA-Z0-9_]+$` (alphanumeric and underscores only)
- **Unique**: Must be unique across all users
- **Case Sensitive**: Yes

### Email Validation
- **Required**: Yes
- **Length**: Maximum 50 characters
- **Pattern**: Valid email format using `@Email` annotation
- **Unique**: Must be unique across all users
- **Case Sensitive**: No (stored in lowercase)

### Password Validation
- **Required**: Yes
- **Length**: 6-40 characters
- **Complexity**: Must contain at least:
  - One uppercase letter (A-Z)
  - One lowercase letter (a-z)
  - One digit (0-9)
- **Pattern**: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$`
- **Storage**: BCrypt hashed with salt

### Name Validation (First/Last)
- **Required**: Yes
- **Length**: 2-50 characters each
- **Pattern**: `^[a-zA-Z\\s]+$` (letters and spaces only)
- **Trim**: Leading/trailing spaces removed

### Phone Number Validation
- **Required**: No (optional)
- **Length**: 10-15 characters when provided
- **Pattern**: `^[+]?[0-9\\s\\-()]+$` (valid phone format)
- **Examples**: `+1234567890`, `(123) 456-7890`, `123-456-7890`

---

## Error Response Format Reference

### Standard API Response Structure
```json
{
    "success": boolean,
    "message": "string",
    "timestamp": "ISO_8601_DATETIME",
    "data": object | null
}
```

### Validation Error Structure
```json
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "ISO_8601_DATETIME",
    "errors": {
        "fieldName": "validation error message",
        "anotherField": "another validation error"
    }
}
```

### Security Error Structure
```json
{
    "status": 401 | 403,
    "error": "Unauthorized" | "Forbidden",
    "message": "error description",
    "path": "/api/endpoint"
}
```

---

## Status Codes Reference

| Status Code | Description | When Used |
|-------------|-------------|-----------|
| 200 | OK | Successful GET, PUT, POST operations |
| 201 | Created | Successful resource creation |
| 400 | Bad Request | Validation errors, business logic errors |
| 401 | Unauthorized | Invalid credentials, missing/invalid token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (username/email exists) |
| 422 | Unprocessable Entity | Semantic validation errors |
| 500 | Internal Server Error | Unexpected server errors |

---

## Rate Limiting & Throttling

### Current Limitations
- No rate limiting implemented (to be added in future versions)

### Recommended Implementation
```yaml
# Future configuration
rate-limiting:
  auth-endpoints:
    signup: 5 requests per minute per IP
    signin: 10 requests per minute per IP
    refresh: 20 requests per minute per user
  user-endpoints: 100 requests per minute per user
  global: 1000 requests per minute per IP
```

---

## Environment-Specific Configuration

### Development Environment
```properties
# Development settings
logging.level.com.hms.authmanagement=DEBUG
jwt.expiration=86400000  # 24 hours
jwt.refresh-expiration=604800000  # 7 days
```

### Production Environment
```properties
# Production settings  
logging.level.com.hms.authmanagement=INFO
jwt.expiration=3600000   # 1 hour
jwt.refresh-expiration=86400000  # 24 hours
```

### Testing Environment
```properties
# Testing settings
logging.level.com.hms.authmanagement=DEBUG
jwt.expiration=300000    # 5 minutes
jwt.refresh-expiration=600000  # 10 minutes
```

---

## Performance Considerations

### Database Optimization
- Indexed fields: `username`, `email`, `created_at`
- Connection pooling configured
- Query optimization for role-based lookups

### Caching Strategy
- JWT validation caching (future enhancement)
- User role caching (future enhancement)
- Database query result caching (future enhancement)

### Monitoring Metrics
- Authentication success/failure rates
- Token validation performance
- Database connection pool usage
- API response times

---

## Troubleshooting Guide

### Common Issues

#### Authentication Failures
```bash
# Check user exists
curl -X GET http://localhost:8081/api/users/username/john_doe \
-H "Authorization: Bearer ADMIN_TOKEN"

# Check user is active
# Look for "isActive": true in response
```

#### Token Issues
```bash
# Validate token
curl -X POST http://localhost:8081/api/auth/validate \
-H "Content-Type: application/json" \
-d '{"token": "YOUR_TOKEN"}'

# Check token expiration
# Decode JWT token payload to check 'exp' claim
```

#### Permission Errors
```bash
# Check user roles
curl -X GET http://localhost:8081/api/users/1 \
-H "Authorization: Bearer ADMIN_TOKEN"

# Verify role assignments in response
```

### Debug Commands
```bash
# Health check
curl http://localhost:8081/actuator/health

# Application info
curl http://localhost:8081/actuator/info

# Check database connectivity
curl http://localhost:8081/actuator/health | jq '.components.db'
```
