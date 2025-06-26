# Patient Management Service API Documentation

## Base URL
```
http://localhost:8082 (Direct)
http://localhost:9000 (Via API Gateway - Recommended)
```

## Overview
The Patient Management Service handles all patient-related operations including patient registration, profile management, medical history, and patient data retrieval. This service requires JWT authentication for all endpoints except health checks.

---

## Patient Data Model

### Patient Entity Structure
```json
{
    "id": 1,
    "userId": 123,
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-05-15",
    "age": 34,
    "gender": "MALE",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "bloodType": "O_POSITIVE",
    "allergies": "Penicillin, Peanuts",
    "medications": "Aspirin 81mg daily",
    "medicalConditions": "Hypertension, Diabetes Type 2",
    "insuranceProvider": "Blue Cross Blue Shield",
    "insurancePolicyNumber": "BC12345678",
    "isActive": true,
    "profileComplete": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
}
```

### Enhanced Features
- **Full Name**: Automatically computed from first and last name
- **Age Calculation**: Dynamically calculated with proper leap year handling
- **Profile Completeness**: Boolean flag indicating if all required fields are filled
- **Soft Delete**: `isActive` flag for data retention
- **Audit Trail**: Creation and update timestamps

### Gender Enum Values
- `MALE`
- `FEMALE`
- `OTHER`

### Blood Type Enum Values
- `A_POSITIVE`, `A_NEGATIVE`
- `B_POSITIVE`, `B_NEGATIVE`
- `AB_POSITIVE`, `AB_NEGATIVE`
- `O_POSITIVE`, `O_NEGATIVE`
- `UNKNOWN`

---

## Authentication
All endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Patient Management Endpoints

### 1. Create Patient
**Endpoint:** `POST /api/patients`

**Description:** Create a new patient record

**Required Roles:** ROLE_ADMIN, ROLE_NURSE, ROLE_PATIENT

**Request Body:**
```json
{
    "userId": 123,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "bloodType": "O_POSITIVE",
    "allergies": "Penicillin, Peanuts",
    "medications": "Aspirin 81mg daily",
    "medicalConditions": "Hypertension",
    "insuranceProvider": "Blue Cross Blue Shield",
    "insurancePolicyNumber": "BC12345678"
}
```

**Enhanced Validation Rules:**
- `userId`: Required, must be unique, corresponds to User ID from Auth Service
- `firstName`: Required, 2-50 characters, letters and spaces only, trimmed
- `lastName`: Required, 2-50 characters, letters and spaces only, trimmed
- `email`: Required, valid email format, must be unique, case-insensitive
- `phoneNumber`: Optional, 10-15 characters, valid international phone format
- `dateOfBirth`: Required, valid date, must be in the past, not future date
- `gender`: Required, must be MALE, FEMALE, or OTHER
- `zipCode`: Optional, 5-10 characters, alphanumeric with optional dash
- `bloodType`: Optional, must be valid enum value
- `allergies`: Optional, max 1000 characters, sanitized for XSS
- `medications`: Optional, max 1000 characters, sanitized for XSS
- `medicalConditions`: Optional, max 1000 characters, sanitized for XSS

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patient created successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "userId": 123,
        "firstName": "John",
        "lastName": "Doe",
        "fullName": "John Doe",
        "email": "john.doe@example.com",
        "phoneNumber": "+1234567890",
        "dateOfBirth": "1990-05-15",
        "age": 34,
        "gender": "MALE",
        "address": "123 Main Street",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA",
        "bloodType": "O_POSITIVE",
        "allergies": "Penicillin, Peanuts",
        "medications": "Aspirin 81mg daily",
        "medicalConditions": "Hypertension",
        "insuranceProvider": "Blue Cross Blue Shield",
        "insurancePolicyNumber": "BC12345678",
        "isActive": true,
        "profileComplete": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
    }
}
```

**Response (Validation Error - 400):**
```json
{
    "success": false,
    "message": "Validation failed",
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "firstName": "First name must be between 2 and 50 characters",
        "email": "Please provide a valid email address",
        "dateOfBirth": "Date of birth must be in the past"
    }
}
```

**Response (Business Logic Error - 400):**
```json
{
    "success": false,
    "message": "Failed to create patient: Patient already exists for user ID: 123",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/patients \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "userId": 123,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "address": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "bloodType": "O_POSITIVE"
}'
```

---

### 2. Get All Patients
**Endpoint:** `GET /api/patients`

**Description:** Retrieve all active patient records with pagination support

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort field (default: createdAt)
- `direction`: Sort direction (asc/desc, default: desc)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patients retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "userId": 123,
            "firstName": "John",
            "lastName": "Doe",
            "fullName": "John Doe",
            "email": "john.doe@example.com",
            "phoneNumber": "+1234567890",
            "dateOfBirth": "1990-05-15",
            "age": 34,
            "gender": "MALE",
            "address": "123 Main Street",
            "city": "New York",
            "state": "NY",
            "zipCode": "10001",
            "country": "USA",
            "bloodType": "O_POSITIVE",
            "allergies": "Penicillin, Peanuts",
            "medications": "Aspirin 81mg daily",
            "medicalConditions": "Hypertension",
            "insuranceProvider": "Blue Cross Blue Shield",
            "insurancePolicyNumber": "BC12345678",
            "isActive": true,
            "profileComplete": true,
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:30:00"
        }
    ],
    "pagination": {
        "page": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1,
        "hasNext": false,
        "hasPrevious": false
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/patients?page=0&size=10&sort=lastName&direction=asc" \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 12. Get Patient Statistics
**Endpoint:** `GET /api/patients/statistics`

**Description:** Get patient statistics and demographics (Admin, Doctor only)

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patient statistics retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "totalPatients": 150,
        "activePatients": 145,
        "inactivePatients": 5,
        "genderDistribution": {
            "MALE": 75,
            "FEMALE": 68,
            "OTHER": 2
        },
        "ageGroups": {
            "0-18": 25,
            "19-35": 45,
            "36-50": 35,
            "51-65": 30,
            "65+": 15
        },
        "bloodTypeDistribution": {
            "O_POSITIVE": 45,
            "A_POSITIVE": 35,
            "B_POSITIVE": 25,
            "AB_POSITIVE": 15,
            "O_NEGATIVE": 12,
            "A_NEGATIVE": 8,
            "B_NEGATIVE": 5,
            "AB_NEGATIVE": 3,
            "UNKNOWN": 2
        },
        "profileCompleteness": {
            "complete": 120,
            "incomplete": 30,
            "completionRate": 80.0
        }
    }
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:9000/api/patients/statistics \
-H "Authorization: Bearer YOUR_DOCTOR_JWT_TOKEN"
```

---

### 13. Get Patient by Username
**Endpoint:** `GET /api/patients/username/{username}`

**Description:** Get patient by their username from Auth Service

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Path Parameters:**
- `username`: Username from Auth Service

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patient found",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "userId": 123,
        "firstName": "John",
        "lastName": "Doe",
        // ... full patient data
    }
}
```

**Response (Not Found - 404):**
```json
{
    "success": false,
    "message": "Patient not found for username: john_doe",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:9000/api/patients/username/john_doe \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Advanced Features

### Validation Enhancements
- **Email Validation**: RFC 5322 compliant email validation
- **Phone Validation**: International phone number format support
- **Date Validation**: Comprehensive date validation with leap year support
- **Text Sanitization**: XSS protection for free text fields
- **Business Rule Validation**: Comprehensive business logic validation

### Performance Optimizations
- **Pagination**: All list endpoints support pagination
- **Caching**: Frequently accessed data cached for performance
- **Database Indexing**: Strategic indexing for common queries
- **Lazy Loading**: Efficient data loading with JPA relationships

### Security Enhancements
- **Input Sanitization**: All input data sanitized and validated
- **SQL Injection Prevention**: Parameterized queries for all database operations
- **Rate Limiting**: API rate limiting to prevent abuse
- **Audit Logging**: Comprehensive audit trail for all operations

---

## Error Handling

### Enhanced Error Responses

**Validation Error (400) - Detailed:**
```json
{
    "success": false,
    "message": "Validation failed",
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "firstName": "First name must be between 2 and 50 characters",
        "email": "Please provide a valid email address",
        "dateOfBirth": "Date of birth must be in the past",
        "phoneNumber": "Invalid phone number format"
    },
    "path": "/api/patients",
    "correlationId": "abc123-def456-ghi789"
}
```

**Business Logic Error (400):**
```json
{
    "success": false,
    "message": "Failed to create patient: Patient already exists for user ID: 123",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/patients",
    "correlationId": "abc123-def456-ghi789"
}
```

**Authentication Error (401):**
```json
{
    "error": "Missing or invalid Authorization header",
    "status": 401,
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/patients",
    "correlationId": "abc123-def456-ghi789"
}
```

**Authorization Error (403):**
```json
{
    "error": "Access Denied: Insufficient permissions",
    "status": 403,
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/patients/123",
    "correlationId": "abc123-def456-ghi789"
}
```

**Not Found Error (404):**
```json
{
    "success": false,
    "message": "Patient not found with ID: 123",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/patients/123",
    "correlationId": "abc123-def456-ghi789"
}
```

**Internal Server Error (500):**
```json
{
    "success": false,
    "message": "Internal server error occurred",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/patients",
    "correlationId": "abc123-def456-ghi789"
}
```

---

## Kafka Integration

### Enhanced Event Handling

**Published Events:**

1. **Patient Registration Event:**
   - **Topic:** `patient.registered`
   - **Trigger:** When a new patient is created
   - **Message Format:**
   ```json
   {
     "eventType": "PATIENT_REGISTERED",
     "patientId": 123,
     "userId": 456,
     "email": "patient@example.com",
     "fullName": "John Doe",
     "timestamp": "2024-01-15T10:30:00"
   }
   ```

2. **Patient Profile Updated Event:**
   - **Topic:** `patient.profile.updated`
   - **Trigger:** When patient profile is updated
   - **Message Format:**
   ```json
   {
     "eventType": "PATIENT_PROFILE_UPDATED",
     "patientId": 123,
     "userId": 456,
     "updatedFields": ["phoneNumber", "address"],
     "timestamp": "2024-01-15T10:30:00"
   }
   ```

**Consumed Events:**

1. **User Patient Creation Event:**
   - **Topic:** `user.patient.created`
   - **Source:** Auth Service when patient user is created
   - **Action:** Automatically creates basic patient profile
   - **Message Format:**
   ```json
   {
     "eventType": "USER_PATIENT_CREATED",
     "userId": 123,
     "username": "patient_doe",
     "email": "patient.doe@example.com",
     "firstName": "John",
     "lastName": "Doe",
     "phoneNumber": "+1234567890",
     "timestamp": "2024-01-15T10:30:00"
   }
   ```

---

## Service Integration

### Enhanced Auth Service Integration
- **Purpose:** Validate user credentials and get user information
- **Endpoints Used:** 
  - `GET /api/users/username/{username}`
  - `GET /api/users/{id}`
  - `POST /api/auth/validate`
- **Function:** `AuthServiceClient.getUserByUsername()`, `AuthServiceClient.getUserById()`
- **Circuit Breaker:** Resilience4j implementation for fault tolerance

### Usage in Patient Service:
```java
// Get patient by username (calls Auth Service internally)
Optional<PatientResponse> patient = patientService.getPatientByUsername("patient_doe");

// Update patient profile by username
PatientResponse updated = patientService.updatePatientProfile("patient_doe", request);

// Validate user exists before creating patient
boolean userExists = authServiceClient.validateUser(userId);
```

---

## Enhanced Data Validation Rules

### Comprehensive Validation
- **Input Sanitization**: HTML tags stripped, special characters escaped
- **Business Logic**: Complex validation rules for medical data
- **Cross-field Validation**: Validation across multiple fields
- **Referential Integrity**: Validation with external services

### Required Fields
- `userId` (must be unique, validated against Auth Service)
- `firstName` (2-50 characters, letters/spaces/hyphens only)
- `lastName` (2-50 characters, letters/spaces/hyphens only)
- `email` (RFC 5322 compliant, unique, case-insensitive)
- `dateOfBirth` (valid date, past date, reasonable age limits)
- `gender` (MALE, FEMALE, OTHER)

### Optional Fields with Enhanced Validation
- `phoneNumber` (E.164 international format when provided)
- `address` (max 500 characters, sanitized)
- `city` (max 50 characters, letters/spaces/hyphens only)
- `state` (max 50 characters, letters/spaces/hyphens only)
- `zipCode` (5-10 characters, country-specific format validation)
- `country` (ISO 3166-1 alpha-2 country codes)
- `bloodType` (valid enum value)
- `allergies`, `medications`, `medicalConditions` (max 1000 characters, sanitized)
- `insuranceProvider` (max 100 characters)
- `insurancePolicyNumber` (max 50 characters, alphanumeric)

### Business Rules
1. **One Patient Per User**: Each userId can have only one patient record
2. **Email Uniqueness**: Email addresses must be unique across all patients (case-insensitive)
3. **Soft Delete**: Patient records are never permanently deleted
4. **Age Calculation**: Age is calculated dynamically with proper leap year handling
5. **Profile Completeness**: System tracks if required fields are completed
6. **Medical Information**: Free text fields allow comprehensive medical information
7. **Insurance Validation**: Insurance information validated for completeness
8. **Data Consistency**: Cross-service validation ensures data consistency

---

## Security Considerations

### Enhanced Data Protection
- **Medical Information**: HIPAA-compliant handling of sensitive medical data
- **Personal Data (PII)**: GDPR-compliant personal information protection
- **Audit Logging**: Comprehensive audit trail for compliance
- **Data Encryption**: Sensitive data encrypted at rest and in transit
- **Access Control**: Fine-grained role-based access control

### Access Control Matrix (Enhanced)

| Operation | Admin | Doctor | Nurse | Patient | Notes |
|-----------|-------|--------|-------|---------|-------|
| Create Patient | ✅ | ✅ | ❌ | Own Only | Doctors can create for referrals |
| View All Patients | ✅ | ✅ | ✅ | ❌ | Paginated results |
| View Patient Details | ✅ | ✅ | ✅ | Own Only | Full medical information |
| Update Medical Info | ✅ | ✅ | ❌ | ❌ | Only medical professionals |
| Update Contact Info | ✅ | ✅ | ✅ | Own Only | Basic demographic updates |
| Update Profile | ✅ | ❌ | ❌ | Own Only | Patients manage own profiles |
| Delete Patient | ✅ | ❌ | ❌ | ❌ | Admin only, soft delete |
| View Statistics | ✅ | ✅ | ❌ | ❌ | Aggregate data only |
| Export Data | ✅ | Limited | ❌ | Own Only | GDPR compliance |

### API Gateway Integration
- All requests go through JWT validation
- User context is automatically added via headers (X-User-Id, X-User-Name, X-User-Roles)
- Rate limiting and CORS protection applied
- Request/response logging for audit purposes

---

## Integration Testing

### Complete Patient Flow Test
```bash
# 1. Register patient user
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{"username": "john_doe", "email": "john@example.com", "password": "Patient123", "firstName": "John", "lastName": "Doe", "roles": ["patient"]}'

# 2. Login as patient
TOKEN=$(curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "john_doe", "password": "Patient123"}' | jq -r '.data.token')

# 3. Get patient profile
curl -X GET http://localhost:9000/api/patients/my-profile \
-H "Authorization: Bearer $TOKEN"

# 4. Update patient profile (partial)
curl -X PUT http://localhost:9000/api/patients/profile/update \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d '{"phoneNumber": "+1234567899", "address": "456 New Address", "bloodType": "A_POSITIVE"}'

# 5. Login as doctor and view patients
DOCTOR_TOKEN=$(curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "dr_smith", "password": "Doctor123"}' | jq -r '.data.token')

curl -X GET http://localhost:9000/api/patients?page=0&size=10 \
-H "Authorization: Bearer $DOCTOR_TOKEN"

# 6. Search patients by name
curl -X GET "http://localhost:9000/api/patients/search?name=John" \
-H "Authorization: Bearer $DOCTOR_TOKEN"

# 7. Get patient statistics
curl -X GET http://localhost:9000/api/patients/statistics \
-H "Authorization: Bearer $DOCTOR_TOKEN"
```
}
```

---

## Kafka Integration

### Published Events

**Patient Registration Event:**
- **Topic:** `patient.registered`
- **Trigger:** When a new patient is created
- **Message:** `"Patient registered: {patientId}"`

### Consumed Events

**User Patient Creation Event:**
- **Topic:** `user.patient.created`
- **Source:** Auth Service when patient user is created
- **Action:** Automatically creates basic patient profile
- **Message Format:**
```json
{
    "userId": 123,
    "username": "patient_doe",
    "email": "patient.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
}
```

---

## Service Integration

### Auth Service Integration
- **Purpose:** Validate user credentials and get user information
- **Endpoint Used:** `GET /api/users/username/{username}`
- **Function:** `AuthServiceClient.getUserByUsername()`

### Usage in Patient Service:
```java
// Get patient by username (calls Auth Service internally)
Optional<PatientResponse> patient = patientService.getPatientByUsername("patient_doe");

// Update patient profile by username
PatientResponse updated = patientService.updatePatientProfile("patient_doe", request);
```

---

## Data Validation Rules

### Required Fields
- `userId` (must be unique)
- `firstName` (2-50 characters)
- `lastName` (2-50 characters)
- `email` (valid email, unique)
- `dateOfBirth` (valid date, past)
- `gender` (MALE, FEMALE, OTHER)

### Optional Fields
- `phoneNumber` (10-15 characters when provided)
- `address`, `city`, `state`, `zipCode`, `country`
- `bloodType` (valid enum value)
- `allergies`, `medications`, `medicalConditions`
- `insuranceProvider`, `insurancePolicyNumber`

### Business Rules
1. Each user can have only one patient record
2. Email addresses must be unique across all patients
3. Soft delete preserves data integrity
4. Age is calculated automatically from date of birth
5. Medical information fields support free text for flexibility

---

## Security Considerations

### Data Protection
- Medical information is sensitive and requires proper role-based access
- Personal data (PII) is protected through authentication and authorization
- Soft delete ensures data retention for medical history

### Access Control
- **Admin:** Full access to all patient records
- **Doctor/Nurse:** Read access to all patients, limited update rights
- **Patient:** Access only to their own record

### API Gateway Integration
- All requests go through JWT validation
- User context is automatically added via headers
- Rate limiting and CORS protection applied

---

## Testing Data

### Create Test Patient
```bash
# First, create a patient user in Auth Service
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "test_patient",
    "email": "test.patient@example.com",
    "password": "Patient123",
    "firstName": "Test",
    "lastName": "Patient",
    "phoneNumber": "+1234567890",
    "roles": ["patient"]
}'

# Then create patient profile (this might be automatic via Kafka)
curl -X POST http://localhost:9000/api/patients \
-H "Authorization: Bearer ADMIN_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "userId": 1,
    "firstName": "Test",
    "lastName": "Patient",
    "email": "test.patient@example.com",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1985-03-20",
    "gender": "OTHER",
    "address": "123 Test Street",
    "city": "Test City",
    "state": "TC",
    "zipCode": "12345",
    "country": "USA",
    "bloodType": "O_POSITIVE"
}'
```

---

## Status Codes Summary

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (Validation/Business Logic Error) |
| 401 | Unauthorized (Missing/Invalid JWT) |
| 403 | Forbidden (Insufficient Permissions) |
| 404 | Not Found (Patient Not Found) |
| 500 | Internal Server Error |

---

## Integration Testing

### Complete Patient Flow Test
```bash
# 1. Register patient user
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{"username": "john_doe", "email": "john@example.com", "password": "Patient123", "firstName": "John", "lastName": "Doe", "roles": ["patient"]}'

# 2. Login as patient
TOKEN=$(curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "john_doe", "password": "Patient123"}' | jq -r '.data.token')

# 3. Get patient profile
curl -X GET http://localhost:9000/api/patients/my-profile \
-H "Authorization: Bearer $TOKEN"

# 4. Update patient profile
curl -X PUT http://localhost:9000/api/patients/profile/update \
-H "Authorization: Bearer $TOKEN" \
-H "Content-Type: application/json" \
-d '{"phoneNumber": "+1234567899", "address": "456 New Address"}'

# 5. Login as doctor and view patient
DOCTOR_TOKEN=$(curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "dr_smith", "password": "Doctor123"}' | jq -r '.data.token')

curl -X GET http://localhost:9000/api/patients \
-H "Authorization: Bearer $DOCTOR_TOKEN"
```
