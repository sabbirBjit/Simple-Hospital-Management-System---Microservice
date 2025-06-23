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
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["patient"]
}
```

**Response (Success - 200):**
```json
{
    "message": "User registered successfully!"
}
```

**Response (Error - 400):**
```json
{
    "message": "Error: Username is already taken!"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "password123",
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
    "password": "password123"
}
```

**Response (Success - 200):**
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_PATIENT"]
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "password": "password123"
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
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_PATIENT"]
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
    "valid": true
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
    "message": "User logged out successfully!"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/logout \
-H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## User Management Endpoints

### 6. Get All Users
**Endpoint:** `GET /api/users`

**Description:** Get list of all users (Admin, Doctor, Nurse only)

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
[
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

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
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

**Path Parameters:**
- `roleName`: admin, doctor, nurse, patient

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```json
[
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

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response (Success - 200):**
```
No content
```

**cURL Command:**
```bash
curl -X DELETE http://localhost:8081/api/users/1 \
-H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN_HERE"
```

---

## Sample Test Data

### Create Admin User
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "admin",
    "email": "admin@hospital.com",
    "password": "admin123",
    "firstName": "System",
    "lastName": "Administrator",
    "phoneNumber": "+1234567890",
    "roles": ["admin"]
}'
```

### Create Doctor User
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_smith",
    "email": "dr.smith@hospital.com",
    "password": "doctor123",
    "firstName": "Dr. Jane",
    "lastName": "Smith",
    "phoneNumber": "+1234567891",
    "roles": ["doctor"]
}'
```

### Create Nurse User
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "nurse_johnson",
    "email": "nurse.johnson@hospital.com",
    "password": "nurse123",
    "firstName": "Mary",
    "lastName": "Johnson",
    "phoneNumber": "+1234567892",
    "roles": ["nurse"]
}'
```

### Create Patient User
```bash
curl -X POST http://localhost:8081/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "patient_doe",
    "email": "patient.doe@example.com",
    "password": "patient123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567893",
    "roles": ["patient"]
}'
```

---

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (Invalid input, username/email taken) |
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
