# Simple Hospital Management System - Microservices Architecture

A comprehensive microservices-based hospital management system built with Spring Boot, featuring role-based authentication, patient management, appointment scheduling, and notification services.

## 🏥 System Overview

This system consists of 5 microservices:
- **API Gateway** (Port 9000) - Centralized routing and authentication
- **Authentication Service** (Port 8081) - User management and JWT authentication
- **Patient Service** (Port 8082) - Patient data and medical history management
- **Appointment Service** (Port 8083) - Appointment scheduling and management
- **Notification Service** (Port 8084) - Email notifications and reminders

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web/Mobile    │    │   API Gateway   │    │  Microservices  │
│     Client      │───▶│   (Port 9000)   │───▶│  (Port 8081-4)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │   Apache Kafka  │    │   MySQL Server  │
                       │  (Event Stream) │    │  (4 Databases)  │
                       └─────────────────┘    └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Apache Kafka 3.0+
- Docker & Docker Compose (optional)

### 1. Database Setup
```sql
CREATE DATABASE hospital_auth_db;
CREATE DATABASE hospital_patient_db;
CREATE DATABASE hospital_appointment_db;
CREATE DATABASE hospital_notification_db;
```

### 2. Environment Setup
Create `.env` file in the root directory:
```env
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=admin123

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT Configuration
JWT_SECRET=bXlTZWNyZXRLZXkxMjM0NTY3ODlteVNlY3JldEtleTEyMzQ1Njc4OW15U2VjcmV0S2V5MTIzNDU2Nzg5bXlTZWNyZXRLZXkxMjM0NTY3ODk=
JWT_EXPIRATION_MS=86400000

# Email Configuration (for notifications)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@hospital.com
```

### 3. Using Docker Compose (Recommended)
```bash
# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f [service-name]
```

### 4. Manual Setup
```bash
# Clone the repository
git clone <repository-url>
cd Simple-Hospital-Management-System---Microservice

# Build all services
mvn clean install -DskipTests

# Start services in order
# 1. Start Kafka and MySQL first
# 2. Start Auth Service
cd auth && mvn spring-boot:run

# 3. Start Patient Service
cd ../patient && mvn spring-boot:run

# 4. Start Appointment Service
cd ../appointment && mvn spring-boot:run

# 5. Start Notification Service
cd ../notification && mvn spring-boot:run

# 6. Start API Gateway
cd ../apigateway && mvn spring-boot:run
```

## 📡 API Access

### Base URLs
- **API Gateway**: http://localhost:9000
- **Auth Service**: http://localhost:8081
- **Patient Service**: http://localhost:8082
- **Appointment Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084

### API Documentation
- [Authentication API](./documentation/auth%20and%20user/auth-api.md)
- [Patient API](./documentation/patient/patient-api.md)
- [Appointment API](./documentation/appointment/appointment-api.md)
- [Notification API](./documentation/notification/notification-api.md)

## 🔐 Authentication

### User Roles
- **ADMIN**: Full system access
- **DOCTOR**: Patient records, appointments, prescriptions
- **NURSE**: Patient records, basic appointment management
- **PATIENT**: Own records, appointment booking

### Getting Started with Authentication
```bash
# 1. Register a new patient
curl -X POST http://localhost:9000/api/auth/signup/patient \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "Password123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890"
}'

# 2. Login to get JWT token
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe",
    "password": "Password123"
}'

# 3. Use the token for authenticated requests
curl -X GET http://localhost:9000/api/patients/my-profile \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
cd auth && mvn test
```

### Test Data
The system includes sample data initialization for testing purposes.

## 📊 Monitoring

### Health Checks
- **API Gateway**: http://localhost:9000/actuator/health
- **Auth Service**: http://localhost:8081/actuator/health
- **Patient Service**: http://localhost:8082/actuator/health
- **Appointment Service**: http://localhost:8083/actuator/health
- **Notification Service**: http://localhost:8084/actuator/health

### Metrics
- **API Gateway**: http://localhost:9000/actuator/metrics
- **Individual Services**: http://localhost:8081-4/actuator/metrics

## 🔧 Configuration

### Service Configuration
Each service has its own configuration file:
- `auth/src/main/resources/application.properties`
- `patient/src/main/resources/application.properties`
- `appointment/src/main/resources/application.yml`
- `notification/src/main/resources/application.yml`
- `apigateway/src/main/resources/application.properties`

### Environment Variables
Key environment variables can be overridden:
- `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure MySQL is running
   - Check database credentials in application.properties
   - Verify database exists

2. **Kafka Connection Failed**
   - Ensure Kafka is running
   - Check Kafka bootstrap servers configuration
   - Verify Kafka topics are created

3. **JWT Token Issues**
   - Ensure JWT secret is consistent across services
   - Check token expiration settings
   - Verify token format in Authorization header

4. **Email Notifications Not Working**
   - Check email credentials in notification service
   - Verify SMTP settings
   - Check firewall settings for SMTP ports

### Logs
```bash
# View service logs
docker-compose logs -f [service-name]

# View specific service logs
cd [service-directory] && mvn spring-boot:run
```

## 📚 Documentation

- [System Requirements](./documentation/requirement.md)
- [Database Schema](./documentation/database/database-schema.md)
- [Security Guide](./documentation/auth%20and%20user/security-guide.md)
- [User Roles](./documentation/auth%20and%20user/user-roles.md)
- [Architecture Diagram](./documentation/Mermaid%20Chart%20-%20Create%20complex,%20visual%20diagrams%20with%20text.%20A%20smarter%20way%20of%20creating%20diagrams.-2025-06-26-074943.mmd)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation in the `documentation/` folder
- Review the troubleshooting section above

---

**Note**: This is a simplified hospital management system for educational purposes. For production use, additional security measures, compliance features, and comprehensive testing would be required. 