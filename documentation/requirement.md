# Simple Hospital Management System - Microservices Architecture

## System Overview
A simplified microservices-based hospital management system with 4 core services, using Kafka as message broker and role-based authentication/authorization.

---

## Service 1: Authentication & User Management Service

### Purpose
Centralized authentication and user management for all system users with role-based access control.

### Core Features
- User registration and login
- JWT token generation and validation
- Role-based access control (Admin, Doctor, Nurse, Patient)
- Password reset functionality
- User profile management
- Session management

### Database Models

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String username;      // unique
    private String email;         // unique
    private String password;      // encrypted
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isEmailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Role> roles;      // Many-to-Many relationship
}
```

#### Role Entity
```java
@Entity
@Table(name = "roles")
public class Role {
    private Long id;
    private RoleName name;        // ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT
    private String description;
}
```

#### Role Enum
```java
public enum RoleName {
    ROLE_ADMIN,
    ROLE_DOCTOR,
    ROLE_NURSE,
    ROLE_PATIENT
}
```

### API Endpoints

#### Authentication Endpoints (/api/auth)
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/validate` - Validate JWT token
- `POST /api/auth/logout` - User logout

#### User Management Endpoints (/api/users)
- `GET /api/users` - Get all users (Admin, Doctor, Nurse only)
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `GET /api/users/role/{roleName}` - Get users by role (Admin only)
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user (Admin only)

### Spring Boot Dependencies (start.spring.io)
```
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Boot DevTools
- MySQL Driver (or PostgreSQL Driver)
- Spring for Apache Kafka
- Validation
- Spring Boot Actuator
```

### Additional Dependencies (Manual)
```xml
<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

### Database Tables
- users
- roles
- user_roles (join table for many-to-many relationship)

---

## Service 2: Patient Management Service

### Purpose
Manage patient information, medical history, and basic patient operations with comprehensive patient data management.

### Core Features
- Patient registration and profile management
- Medical history and conditions tracking
- Emergency contact information
- Patient search and filtering
- Basic medical record keeping
- Insurance information management
- Blood type and allergy tracking
- Medication management
- Patient demographic information
- Age calculation and profile completeness tracking

### Database Models

#### Patient Entity
```java
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long userId;              // Reference to User from Auth Service
    
    @Column(nullable = false, length = 50)
    private String firstName;
    
    @Column(nullable = false, length = 50)
    private String lastName;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(length = 15)
    private String phoneNumber;
    
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    @Enumerated(EnumType.STRING)
    private BloodType bloodType;
    
    @Column(columnDefinition = "TEXT")
    private String allergies;
    
    @Column(columnDefinition = "TEXT")
    private String medications;
    
    @Column(columnDefinition = "TEXT")
    private String medicalConditions;
    
    private String insuranceProvider;
    private String insurancePolicyNumber;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    
    // Calculated fields
    @Transient
    private Integer age;
    
    // Helper methods
    public String getFullName() { return firstName + " " + lastName; }
    public boolean isProfileComplete() { /* validation logic */ }
}
```

#### MedicalHistory Entity
```java
@Entity
@Table(name = "medical_history")
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @Column(nullable = false)
    private String condition;
    
    private String diagnosis;
    
    @Column(nullable = false)
    private LocalDate diagnosisDate;
    
    private String treatment;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
```

#### EmergencyContact Entity
```java
@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String relationship;
    
    @Column(nullable = false, length = 15)
    private String phoneNumber;
    
    @Column(length = 100)
    private String email;
    
    private String address;
    
    @Column(nullable = false)
    private Boolean isPrimary = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
```

#### Gender Enum
```java
public enum Gender {
    MALE,
    FEMALE,
    OTHER
}
```

#### BloodType Enum
```java
public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    UNKNOWN("Unknown");
}
```

### API Endpoints

#### Patient Management (/api/patients)
- `POST /api/patients` - Create new patient
- `GET /api/patients` - Get all patients (Admin, Doctor, Nurse)
- `GET /api/patients/{id}` - Get patient by ID
- `GET /api/patients/user/{userId}` - Get patient by User ID
- `GET /api/patients/search?name={name}` - Search patients by name
- `GET /api/patients/city/{city}` - Get patients by city
- `PUT /api/patients/{id}` - Update patient (Admin, Nurse, own record)
- `DELETE /api/patients/{id}` - Soft delete patient (Admin only)

#### Patient Profile Management (/api/patients/profile)
- `GET /api/patients/my-profile` - Get own profile (Patient)
- `PUT /api/patients/profile/update` - Partial profile update (Patient)
- `PUT /api/patients/profile/full-update` - Full profile update (Patient)

#### Future Endpoints (Medical History & Emergency Contacts)
- `GET /api/patients/{id}/medical-history` - Get medical history
- `POST /api/patients/{id}/medical-history` - Add medical history entry
- `GET /api/patients/{id}/emergency-contacts` - Get emergency contacts
- `POST /api/patients/{id}/emergency-contacts` - Add emergency contact

### Spring Boot Dependencies (start.spring.io)
```
- Spring Web
- Spring Data JPA
- Spring Boot DevTools
- MySQL Driver (or PostgreSQL Driver)
- Spring for Apache Kafka
- Validation
- Spring Boot Actuator
- Spring Security (for JWT validation)
```

### Additional Dependencies (Manual)
```xml
<!-- JWT Support for token validation -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
<!-- Lombok for reducing boilerplate code -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### Key Features
1. **Automatic Patient Profile Creation**: When a user with ROLE_PATIENT is created in Auth Service, a basic patient profile is automatically created via Kafka events
2. **Comprehensive Medical Information**: Stores allergies, medications, medical conditions, and insurance details
3. **Age Calculation**: Age is automatically calculated from date of birth with proper leap year handling
4. **Soft Delete**: Patient records are never permanently deleted to maintain medical history integrity
5. **Role-based Access**: Different access levels for different user roles
6. **Integration with Auth Service**: Validates users and retrieves user information when needed
7. **Medical History Tracking**: Detailed medical history with timeline and treatment information
8. **Emergency Contact Management**: Multiple emergency contacts with relationship tracking
9. **Profile Completeness**: System tracks and validates profile completion status
10. **Advanced Search**: Name-based search with partial matching capabilities

### Database Tables
- patients (main patient information)
- medical_history (patient medical history records)
- emergency_contacts (patient emergency contact information)

---

## Service 3: Appointment Management Service

### Purpose
Handle appointment scheduling, doctor availability, and appointment lifecycle management.

### Core Features
- Book, reschedule, and cancel appointments
- Doctor availability management
- Time slot management
- Appointment status tracking
- Basic reminder system

### Spring Boot Dependencies (start.spring.io)
```
- Spring Web
- Spring Data JPA
- Spring Boot DevTools
- MySQL Driver (or PostgreSQL Driver)
- Spring for Apache Kafka
- Validation
- Spring Boot Actuator
- Spring Security (for JWT validation)
```

### Additional Dependencies (Manual)
```xml
<!-- JWT Support for token validation -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

### Database Tables
- appointments
- doctor_availability
- time_slots

---

## Service 4: Notification Service

### Purpose
Handle all system notifications including appointment reminders and system alerts.

### Core Features
- Send appointment reminders
- Email notifications
- System alerts
- Notification templates
- Notification history

### Spring Boot Dependencies (start.spring.io)
```
- Spring Web
- Spring Data JPA
- Spring Boot DevTools
- MySQL Driver (or PostgreSQL Driver)
- Spring for Apache Kafka
- Spring Boot Starter Mail
- Validation
- Spring Boot Actuator
```

### Database Tables
- notifications
- notification_templates
- notification_history

---

## Inter-Service Communication

### Kafka Topics
- `user.created` - When new user is registered
- `user.patient.created` - When new patient user is registered (specific to patient role)
- `patient.registered` - When new patient profile is created
- `appointment.booked` - When appointment is scheduled
- `appointment.cancelled` - When appointment is cancelled
- `appointment.reminder` - For sending reminders

### Service Integration Patterns

#### Auth Service → Patient Service
- **Method**: Kafka Events + REST API
- **Events**: `user.patient.created` → Automatic patient profile creation
- **REST**: Patient service calls Auth service to validate users

#### Patient Service → Auth Service
- **Method**: REST API calls
- **Usage**: Validate user existence, get user details by username
- **Endpoint**: `GET /api/users/username/{username}`

### API Gateway
Spring Cloud Gateway as an entry point for all services running on port 9000.

**Gateway Dependencies:**
```
- Gateway
- Spring Boot DevTools
- Spring Boot Actuator
- Spring Security (for JWT validation)
- WebFlux
```

---

## Security Configuration

### JWT Flow
1. User authenticates via Authentication Service
2. JWT token issued with user roles and claims (userId, email, firstName, lastName)
3. Other services validate JWT tokens via API Gateway
4. API Gateway adds user context headers (X-User-Id, X-User-Name) for downstream services
5. Role-based access control applied at method level

### Role Hierarchy
- **ADMIN**: Full system access
- **DOCTOR**: Patient records, appointments, prescriptions
- **NURSE**: Patient records, basic appointment management
- **PATIENT**: Own records, appointment booking

### Security Features
- **Stateless Authentication**: JWT tokens with claims
- **Token Refresh**: Automatic token renewal system
- **Role-based Authorization**: Method-level security annotations
- **API Gateway Security**: Centralized authentication and authorization
- **Secure Headers**: User context forwarded securely between services

---

## Development Sequence Recommendation

1. **Start with Authentication Service** - Core foundation
   - Implement user management and JWT authentication
   - Set up role-based access control
   - Configure Kafka for user events

2. **Patient Management Service** - Basic data management
   - Implement patient CRUD operations
   - Set up Kafka consumer for user events
   - Implement role-based patient access

3. **API Gateway Service** - Security and routing
   - Configure routing to Auth and Patient services
   - Implement JWT validation
   - Set up CORS and security headers

4. **Appointment Management Service** - Business logic
   - Implement appointment scheduling
   - Integrate with Patient service for patient validation
   - Set up appointment notifications

5. **Notification Service** - Supporting service
   - Implement email notifications
   - Set up Kafka consumers for appointment events
   - Create notification templates

---

## Configuration

### Service-Specific Configurations

#### Authentication Service - application.properties
```properties
# Server Configuration
server.port=8081

# Application Configuration
spring.application.name=auth-service

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_auth_db
spring.datasource.username=root
spring.datasource.password=admin123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT Configuration
jwt.secret=bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=
jwt.expirationMs=86400000
jwt.refreshExpirationMs=604800000

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Management Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Logging Configuration
logging.level.com.hms.auth=DEBUG
```

#### Patient Management Service - application.properties
```properties
# Server Configuration
server.port=8082

# Application Configuration
spring.application.name=patient-service

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_patient_db
spring.datasource.username=root
spring.datasource.password=admin123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=patient-service-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# JWT Configuration for token validation
jwt.secret=bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=

# External Service URLs
services.auth-service.url=http://localhost:8081

# Management Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Logging Configuration
logging.level.com.hms.patient=DEBUG
```

#### Appointment Management Service - application.yml
```yaml
server:
  port: 8083

spring:
  application:
    name: appointment-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_appointment_db
    username: root
    password: admin123
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: appointment-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# JWT Configuration for token validation
jwt:
  secret: bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=

# External Service URLs
services:
  auth-service:
    url: http://localhost:8081
  patient-service:
    url: http://localhost:8082

# Appointment Configuration
appointment:
  slot-duration: 30 # minutes
  advance-booking-days: 30
  reminder-hours: 24

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.hms.appointment: DEBUG
```

#### Notification Service - application.yml
```yaml
server:
  port: 8084

spring:
  application:
    name: notification-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_notification_db
    username: root
    password: admin123
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: notification-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
  
  # Email Configuration
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: 5000
          timeout: 3000
          writetimeout: 5000

# Notification Configuration
notification:
  email:
    from: ${MAIL_FROM:noreply@hospital.com}
    templates:
      appointment-reminder: "appointment-reminder.html"
      appointment-confirmation: "appointment-confirmation.html"
      welcome: "welcome.html"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.hms.notification: DEBUG
    org.springframework.mail: DEBUG
```

#### API Gateway Service - application.properties
```properties
# Server Configuration
server.port=9000

# Application Configuration
spring.application.name=api-gateway

# Disable the RouteLocator bean routes since we're using properties - Updated format
spring.cloud.gateway.server.webflux.discovery.locator.enabled=false

# Gateway Routes Configuration - Updated format
spring.cloud.gateway.server.webflux.routes[0].id=auth-service
spring.cloud.gateway.server.webflux.routes[0].uri=http://localhost:8081
spring.cloud.gateway.server.webflux.routes[0].predicates[0]=Path=/api/auth/**

spring.cloud.gateway.server.webflux.routes[1].id=user-service
spring.cloud.gateway.server.webflux.routes[1].uri=http://localhost:8081
spring.cloud.gateway.server.webflux.routes[1].predicates[0]=Path=/api/users/**

spring.cloud.gateway.server.webflux.routes[2].id=patient-service
spring.cloud.gateway.server.webflux.routes[2].uri=http://localhost:8082
spring.cloud.gateway.server.webflux.routes[2].predicates[0]=Path=/api/patients/**

spring.cloud.gateway.server.webflux.routes[3].id=appointment-service
spring.cloud.gateway.server.webflux.routes[3].uri=http://localhost:8083
spring.cloud.gateway.server.webflux.routes[3].predicates[0]=Path=/api/appointments/**

spring.cloud.gateway.server.webflux.routes[4].id=notification-service
spring.cloud.gateway.server.webflux.routes[4].uri=http://localhost:8084
spring.cloud.gateway.server.webflux.routes[4].predicates[0]=Path=/api/notifications/**

# CORS Configuration
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedOrigins=http://localhost:3000,http://localhost:9000
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedMethods=GET,POST,PUT,DELETE,OPTIONS
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowedHeaders=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowCredentials=true

# JWT Configuration for gateway-level authentication
jwt.secret=bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=

# Auth Service Configuration
auth.service.url=http://localhost:8081

# Management Configuration
management.endpoints.web.exposure.include=health,info,metrics,gateway
management.endpoint.health.show-details=always

# Logging Configuration
logging.level.com.hms.apigateway=DEBUG
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Access URLs
- **API Gateway**: http://localhost:9000
- **Auth Service Direct**: http://localhost:8081
- **Patient Service Direct**: http://localhost:8082
- **Appointment Service Direct**: http://localhost:8083
- **Notification Service Direct**: http://localhost:8084

### Gateway Routes
All services are accessible through the API Gateway:
- **Authentication**: http://localhost:9000/api/auth/*
- **User Management**: http://localhost:9000/api/users/*
- **Patient Management**: http://localhost:9000/api/patients/*
- **Appointment Management**: http://localhost:9000/api/appointments/*
- **Notifications**: http://localhost:9000/api/notifications/*

### Database Setup
Create the following databases in MySQL:
```sql
CREATE DATABASE hospital_auth_db;
CREATE DATABASE hospital_patient_db;
CREATE DATABASE hospital_appointment_db;
CREATE DATABASE hospital_notification_db;
```

### Prerequisites
- Java 21
- MySQL 8.0+
- Apache Kafka 2.8+
- Maven 3.8+
- Postman or similar for API testing