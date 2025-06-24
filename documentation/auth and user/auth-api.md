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

### 2. User Login
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

### 3. Refresh Token
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

### 4. Validate Token
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

### 5. User Logout
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

### 6. Get All Users
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
        }
    ]
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/users \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

### 7. Get User by ID
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

### 8. Get Users by Role
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

### 9. Update User
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

### 10. Delete User
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
- `POST http://localhost:9000/api/auth/refresh`
- `POST http://localhost:9000/api/auth/validate`
- `POST http://localhost:9000/api/auth/logout`

**Gateway Authentication Flow:**
1. Login via gateway to get JWT token
2. Include `Authorization: Bearer <token>` header for protected routes
3. Gateway validates token before forwarding to services
4. Invalid tokens return 401 Unauthorized

---

## Sample Test Data

### Create Admin User
```bash
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
```

### Create Doctor User
```bash
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_smith",
    "email": "dr.smith@hospital.com",
    "password": "Doctor123",
    "firstName": "Dr. Jane",
    "lastName": "Smith",
    "phoneNumber": "+1234567891",
    "roles": ["doctor"]
}'
```

### Create Nurse User
```bash
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "nurse_johnson",
    "email": "nurse.johnson@hospital.com",
    "password": "Nurse123",
    "firstName": "Mary",
    "lastName": "Johnson",
    "phoneNumber": "+1234567892",
    "roles": ["nurse"]
}'
```

### Create Patient User
```bash
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_doe",
    "email": "patient.doe@example.com",
    "password": "Patient123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567893",
    "roles": ["patient"]
}'
```

---

## Validation Rules Summary

### Username Validation
- Required
- 3-20 characters
- Alphanumeric and underscores only
- Must be unique

### Email Validation
- Required
- Valid email format
- Maximum 50 characters
- Must be unique

### Password Validation
- Required
- 6-40 characters
- Must contain at least:
  - One uppercase letter
  - One lowercase letter
  - One number

### Name Validation
- First and last names required
- 2-50 characters each
- Letters and spaces only

### Phone Number Validation
- Optional
- 10-15 characters when provided
- Valid phone number format

---

## Error Response Format

### Validation Errors (400)
```json
{
    "message": "Validation failed",
    "status": 400,
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "fieldName": "Error message"
    }
}
```

### Business Logic Errors (400)
```json
{
    "success": false,
    "message": "Specific error message",
    "timestamp": "2024-01-15T10:30:00",
    "data": null
}
```

### Authentication Errors (401)
```json
{
    "message": "Invalid username or password",
    "status": 401,
    "timestamp": "2024-01-15T10:30:00",
    "errors": null
}
```

---

## Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (Validation errors, business logic errors) |
| 401 | Unauthorized (Invalid credentials, expired token) |
| 403 | Forbidden (Insufficient permissions) |
| 404 | Not Found (User not found) |
| 500 | Internal Server Error |

---

## Authentication Flow

1. **Register** → Use `/api/auth/signup` to create account
2. **Login** → Use `/api/auth/signin` to get JWT tokens
3. **Access Protected Endpoints** → Include `Authorization: Bearer <token>` header
4. **Refresh Token** → Use `/api/auth/refresh` when access token expires
5. **Logout** → Use `/api/auth/logout` and remove tokens from client

### JWT Token Information
- **Access Token Expiration**: 24 hours (86400000 ms)
- **Refresh Token Expiration**: 7 days (604800000 ms)
- **Algorithm**: HS256
- **Token Type**: Bearer
