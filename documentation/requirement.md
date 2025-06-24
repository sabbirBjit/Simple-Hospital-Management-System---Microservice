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
Manage patient information, medical history, and basic patient operations.

### Core Features
- Patient registration and profile management
- Medical history storage
- Emergency contact information
- Patient search and filtering
- Basic medical record keeping

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
- patients
- medical_history
- emergency_contacts

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
- `appointment.booked` - When appointment is scheduled
- `appointment.cancelled` - When appointment is cancelled
- `appointment.reminder` - For sending reminders
- `patient.registered` - When new patient is added

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
2. JWT token issued with user roles
3. Other services validate JWT tokens
4. Role-based access control applied

### Role Hierarchy
- **ADMIN**: Full system access
- **DOCTOR**: Patient records, appointments, prescriptions
- **NURSE**: Patient records, basic appointment management
- **PATIENT**: Own records, appointment booking

---

## Development Sequence Recommendation

1. **Start with Authentication Service** - Core foundation
2. **Patient Management Service** - Basic data management
3. **Appointment Management Service** - Business logic
4. **Notification Service** - Supporting service

---

## Configuration

### Service-Specific Configurations

#### Authentication Service - application.yml
```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_auth_db
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
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# JWT Configuration
jwt:
  secret: bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=
  expiration: 86400000 # 24 hours in milliseconds
  refresh-expiration: 604800000 # 7 days in milliseconds

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
    com.hms.auth: DEBUG
    org.springframework.security: DEBUG
```

#### Patient Management Service - application.yml
```yaml
server:
  port: 8082

spring:
  application:
    name: patient-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/hospital_patient_db
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
      group-id: patient-service-group
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
    com.hms.patient: DEBUG
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