# Patient Management Service - Comprehensive Documentation

## Service Overview

The Patient Management Service is a core microservice in the Hospital Management System responsible for managing all patient-related data and operations. It provides comprehensive patient profile management, medical information tracking, and serves as the central repository for patient data across the system.

### Key Responsibilities

- **Patient Profile Management**: Complete patient demographic and contact information
- **Medical Information**: Medical history, conditions, allergies, and medications
- **Insurance Management**: Insurance provider and policy information
- **Data Integration**: Seamless integration with Auth Service for user management
- **Event Handling**: Kafka-based event processing for system-wide notifications
- **Access Control**: Role-based access control for patient data security

---

## Architecture Overview

### Service Architecture
```
┌─────────────────────┐
│   API Gateway       │
│   (Port 9000)       │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│  Patient Service    │
│   (Port 8082)       │
├─────────────────────┤
│ • REST Controllers  │
│ • Service Layer     │
│ • Data Access Layer │
│ • Kafka Integration │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│   MySQL Database    │
│ hospital_patient_db │
└─────────────────────┘
```

### Integration Points
- **Auth Service**: User authentication and profile integration
- **API Gateway**: Request routing and JWT validation
- **Kafka**: Event-driven communication with other services
- **MySQL Database**: Persistent data storage

---

## Data Models

### Core Entities

#### Patient Entity
```java
@Entity
@Table(name = "patients")
public class Patient {
    // Identity
    private Long id;                    // Primary key
    private Long userId;                // Auth service user reference
    
    // Personal Information
    private String firstName;           // Required
    private String lastName;            // Required
    private String email;               // Required, unique
    private String phoneNumber;         // Optional
    private LocalDate dateOfBirth;      // Required
    private Gender gender;              // Required enum
    
    // Address Information
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Medical Information
    private BloodType bloodType;        // Enum
    private String allergies;           // Free text
    private String medications;         // Free text
    private String medicalConditions;   // Free text
    
    // Insurance Information
    private String insuranceProvider;
    private String insurancePolicyNumber;
    
    // System Fields
    private Boolean isActive;           // Soft delete flag
    private LocalDateTime createdAt;    // Auto-generated
    private LocalDateTime updatedAt;    // Auto-updated
    
    // Relationships
    private List<MedicalHistory> medicalHistory;
    private List<EmergencyContact> emergencyContacts;
    
    // Calculated Fields
    public int getAge() { /* calculation */ }
    public String getFullName() { /* firstName + lastName */ }
}
```

#### Supporting Enums
```java
public enum Gender {
    MALE, FEMALE, OTHER
}

public enum BloodType {
    A_POSITIVE("A+"), A_NEGATIVE("A-"),
    B_POSITIVE("B+"), B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"), O_NEGATIVE("O-"),
    UNKNOWN("Unknown")
}
```

### Future Entities (Planned)

#### MedicalHistory Entity
```java
@Entity
@Table(name = "medical_history")
public class MedicalHistory {
    private Long id;
    private Long patientId;
    private String condition;
    private String diagnosis;
    private LocalDate diagnosisDate;
    private String treatment;
    private String notes;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
```

#### EmergencyContact Entity
```java
@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {
    private Long id;
    private Long patientId;
    private String name;
    private String relationship;
    private String phoneNumber;
    private String email;
    private String address;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
```

---

## API Endpoints Overview

### Patient Management Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/patients` | Create new patient | Admin, Nurse, Patient |
| GET | `/api/patients` | Get all patients | Admin, Doctor, Nurse |
| GET | `/api/patients/{id}` | Get patient by ID | Admin, Doctor, Nurse |
| GET | `/api/patients/user/{userId}` | Get patient by User ID | Admin, Doctor, Nurse, Own |
| GET | `/api/patients/search` | Search patients by name | Admin, Doctor, Nurse |
| GET | `/api/patients/city/{city}` | Get patients by city | Admin, Doctor, Nurse |
| PUT | `/api/patients/{id}` | Update patient | Admin, Nurse, Own |
| DELETE | `/api/patients/{id}` | Soft delete patient | Admin |

### Patient Profile Endpoints (Patient-Only)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/patients/my-profile` | Get own profile | Patient |
| PUT | `/api/patients/profile/update` | Partial profile update | Patient |
| PUT | `/api/patients/profile/full-update` | Full profile update | Patient |

---

## Business Logic

### Patient Registration Flow
1. **User Creation**: User registered in Auth Service with ROLE_PATIENT
2. **Kafka Event**: Auth Service publishes `user.patient.created` event
3. **Profile Creation**: Patient Service creates basic patient profile
4. **Profile Completion**: Patient completes profile information
5. **Notification**: System sends welcome notification

### Profile Update Logic
- **Partial Updates**: Only provided fields are updated
- **Full Updates**: All fields must be provided
- **Validation**: Business rules and constraints enforced
- **Email Uniqueness**: Prevents duplicate email addresses
- **Audit Trail**: All changes logged with timestamps

### Data Validation Rules

#### Required Fields
- `userId`: Must be unique, references Auth Service user
- `firstName`: 2-50 characters, letters and spaces only
- `lastName`: 2-50 characters, letters and spaces only
- `email`: Valid email format, must be unique across all patients
- `dateOfBirth`: Valid date, must be in the past
- `gender`: Must be one of: MALE, FEMALE, OTHER

#### Optional Fields with Validation
- `phoneNumber`: 10-15 characters when provided, valid phone format
- `zipCode`: 5-10 characters, alphanumeric
- `bloodType`: Must be valid enum value
- `allergies`, `medications`, `medicalConditions`: Free text, max 1000 characters

#### Business Rules
1. **One Patient Per User**: Each userId can have only one patient record
2. **Email Uniqueness**: Email addresses must be unique across all patients
3. **Soft Delete**: Patient records are never permanently deleted
4. **Age Calculation**: Age is calculated dynamically from date of birth
5. **Profile Completeness**: System tracks if required fields are completed

---

## Security Implementation

### Authentication & Authorization
- **JWT Validation**: All endpoints require valid JWT tokens
- **Role-Based Access**: Different permissions for different user roles
- **Resource Ownership**: Patients can only access their own records
- **API Gateway Integration**: Centralized authentication handling

### Data Protection
- **Sensitive Information**: Medical data protected with appropriate access controls
- **PII Security**: Personal information handled according to privacy regulations
- **Audit Logging**: All access and modifications logged for compliance
- **Soft Delete**: Maintains data integrity for medical history

### Access Control Matrix

| Operation | Admin | Doctor | Nurse | Patient |
|-----------|-------|--------|-------|---------|
| Create Patient | ✅ | ✅ | ❌ | Own Only |
| View All Patients | ✅ | ✅ | ✅ | ❌ |
| View Patient Details | ✅ | ✅ | ✅ | Own Only |
| Update Medical Info | ✅ | ✅ | ❌ | ❌ |
| Update Contact Info | ✅ | ✅ | ✅ | Own Only |
| Delete Patient | ✅ | ❌ | ❌ | ❌ |

---

## Integration with Other Services

### Auth Service Integration
```java
@Component
public class AuthServiceClient {
    public Optional<UserDto> getUserByUsername(String username) {
        // REST call to Auth Service
        // Returns user information for patient lookup
    }
}
```

**Usage Scenarios:**
- Get patient by username
- Update patient profile by username
- Validate user existence for patient creation

### Kafka Event Integration

#### Published Events
```java
// Patient registration event
kafkaTemplate.send("patient.registered", "Patient registered: " + patientId);
```

#### Consumed Events
```java
@KafkaListener(topics = "user.patient.created")
public void handlePatientUserCreated(String message) {
    // Automatically create patient profile
    // Parse user information and create basic patient record
}
```

---

## Database Design

### Table Structure
```sql
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    address VARCHAR(500),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50),
    blood_type ENUM('A_POSITIVE', 'A_NEGATIVE', 'B_POSITIVE', 'B_NEGATIVE', 
                   'AB_POSITIVE', 'AB_NEGATIVE', 'O_POSITIVE', 'O_NEGATIVE', 'UNKNOWN'),
    allergies TEXT,
    medications TEXT,
    medical_conditions TEXT,
    insurance_provider VARCHAR(100),
    insurance_policy_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_email (email),
    INDEX idx_active (is_active),
    INDEX idx_city (city),
    INDEX idx_created_at (created_at)
);
```

### Performance Optimizations
- **Indexing**: Strategic indexes for common queries
- **Connection Pool**: HikariCP for efficient database connections
- **Query Optimization**: Efficient JPA queries with proper fetch strategies
- **Caching**: Application-level caching for frequently accessed data

---

## Configuration & Deployment

### Service Configuration
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_patient_db
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:admin123}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=${JPA_SHOW_SQL:false}

# Kafka Configuration
spring.kafka.bootstrap-servers=${KAFKA_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=patient-service-group

# External Service URLs
services.auth-service.url=${AUTH_SERVICE_URL:http://localhost:8081}

# JWT Configuration
jwt.secret=${JWT_SECRET:your-secret-key}
```

### Environment Variables
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `KAFKA_SERVERS`: Kafka bootstrap servers
- `AUTH_SERVICE_URL`: Auth service URL
- `JWT_SECRET`: JWT signing secret

---

## Monitoring & Observability

### Health Checks
```java
@Component
public class PatientServiceHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check database connectivity
        // Check external service availability
        // Return health status
    }
}
```

### Metrics
- **Request Metrics**: Response times, success rates
- **Database Metrics**: Connection pool status, query performance
- **Business Metrics**: Patient registration rates, profile completion
- **Error Metrics**: Error rates, exception tracking

### Logging Strategy
```java
// Structured logging for patient operations
logger.info("Patient created successfully: patientId={}, userId={}", 
           patientId, userId);
logger.warn("Failed to update patient profile: userId={}, error={}", 
           userId, error.getMessage());
```

---

## Testing Strategy

### Unit Testing
- Service layer methods
- Validation logic
- Business rule enforcement
- Error handling scenarios

### Integration Testing
- Database operations
- Kafka event handling
- External service communication
- End-to-end API flows

### Test Data Management
```java
@TestConfiguration
public class PatientTestDataConfig {
    public Patient createTestPatient() {
        // Create test patient with all required fields
        // Return patient for testing scenarios
    }
}
```

---

## Future Enhancements

### Planned Features
1. **Medical History Tracking**: Detailed medical history with timeline
2. **Emergency Contacts**: Multiple emergency contact management
3. **Document Management**: Medical document storage and retrieval
4. **Appointment Integration**: Enhanced appointment history
5. **Notification Preferences**: Customizable notification settings

### Technical Improvements
1. **Caching Strategy**: Redis-based caching for performance
2. **Search Enhancement**: Elasticsearch integration for advanced search
3. **Data Analytics**: Patient data analytics and reporting
4. **Mobile API**: Mobile-optimized API endpoints
5. **Audit Logging**: Comprehensive audit trail implementation

---

## Troubleshooting Guide

### Common Issues

#### Patient Creation Failures
```
Error: "Patient already exists for user ID"
Solution: Check if patient record already exists, use update instead
```

#### Email Validation Errors
```
Error: "Email is already in use by another patient"
Solution: Verify email uniqueness, use different email address
```

#### Profile Update Issues
```
Error: "Patient profile not found for user ID"
Solution: Ensure patient record exists, create if necessary
```

### Debug Endpoints
```bash
# Check patient status
GET /api/patients/user/{userId}

# Validate patient data
GET /api/patients/{id}

# Check service health
GET /actuator/health
```

---

## Best Practices

### Development Guidelines
1. **Input Validation**: Always validate input at service layer
2. **Error Handling**: Use proper exception handling with meaningful messages
3. **Transaction Management**: Use `@Transactional` for data consistency
4. **Security**: Always validate user permissions before data access
5. **Performance**: Use appropriate fetch strategies and caching

### API Design Principles
1. **RESTful Design**: Follow REST conventions consistently
2. **Consistent Responses**: Use standardized response formats
3. **Error Codes**: Use appropriate HTTP status codes
4. **Documentation**: Keep API documentation up to date
5. **Versioning**: Plan for API versioning strategy

### Security Best Practices
1. **Data Validation**: Validate all input data thoroughly
2. **Access Control**: Implement proper role-based permissions
3. **Audit Logging**: Log all sensitive operations
4. **Error Messages**: Don't expose sensitive information in errors
5. **Token Validation**: Always validate JWT tokens properly
