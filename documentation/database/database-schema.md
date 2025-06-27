# Hospital Management System - Database Schema Documentation

## Overview
This document outlines the complete database schema for the Hospital Management System microservices, including table structures, relationships, and constraints for all services.

---

## Database Structure Overview

The system uses separate databases for each microservice to ensure proper data isolation and service autonomy:

- **hospital_auth_db**: Authentication and user management
- **hospital_patient_db**: Patient information and medical records
- **hospital_appointment_db**: Appointment scheduling and management
- **hospital_notification_db**: Notification system and templates

---

## Authentication Service Database (hospital_auth_db)

### users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15),
    is_active BOOLEAN DEFAULT TRUE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_active (is_active),
    INDEX idx_created_at (created_at)
);
```

### roles Table
```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name ENUM('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_NURSE', 'ROLE_PATIENT') UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### user_roles Table (Junction Table)
```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    INDEX idx_assigned_at (assigned_at)
);
```

### refresh_tokens Table
```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expiry_date (expiry_date)
);
```

### password_reset_tokens Table
```sql
CREATE TABLE password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_token (token),
    INDEX idx_expiry_date (expiry_date)
);
```

### Default Roles Data
```sql
INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System Administrator with full access'),
('ROLE_DOCTOR', 'Medical Doctor with patient management access'),
('ROLE_NURSE', 'Registered Nurse with limited patient access'),
('ROLE_PATIENT', 'Patient with access to own records');
```

---

## Patient Service Database (hospital_patient_db)

### patients Table
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
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(15),
    emergency_contact_relationship VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    profile_complete BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_email (email),
    INDEX idx_first_name (first_name),
    INDEX idx_last_name (last_name),
    INDEX idx_city (city),
    INDEX idx_blood_type (blood_type),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at),
    INDEX idx_date_of_birth (date_of_birth),
    
    CONSTRAINT chk_date_of_birth CHECK (date_of_birth <= CURDATE()),
    CONSTRAINT chk_zip_code CHECK (LENGTH(zip_code) >= 5 OR zip_code IS NULL)
);
```

### medical_history Table
```sql
CREATE TABLE medical_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    condition_name VARCHAR(200) NOT NULL,
    diagnosis TEXT,
    diagnosis_date DATE NOT NULL,
    treatment TEXT,
    doctor_name VARCHAR(100),
    hospital_name VARCHAR(100),
    notes TEXT,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'),
    status ENUM('ACTIVE', 'RESOLVED', 'CHRONIC', 'MONITORING') DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_diagnosis_date (diagnosis_date),
    INDEX idx_condition_name (condition_name),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);
```

### emergency_contacts Table
```sql
CREATE TABLE emergency_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(500),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    country VARCHAR(50),
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_name (name),
    INDEX idx_relationship (relationship),
    INDEX idx_is_primary (is_primary),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);
```

### patient_medications Table
```sql
CREATE TABLE patient_medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    medication_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    prescribed_by VARCHAR(100),
    reason VARCHAR(500),
    side_effects TEXT,
    status ENUM('ACTIVE', 'DISCONTINUED', 'COMPLETED', 'ON_HOLD') DEFAULT 'ACTIVE',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_medication_name (medication_name),
    INDEX idx_start_date (start_date),
    INDEX idx_status (status),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);
```

### patient_allergies Table
```sql
CREATE TABLE patient_allergies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    allergen VARCHAR(200) NOT NULL,
    allergy_type ENUM('FOOD', 'DRUG', 'ENVIRONMENTAL', 'CONTACT', 'OTHER') NOT NULL,
    severity ENUM('MILD', 'MODERATE', 'SEVERE', 'LIFE_THREATENING') NOT NULL,
    reaction_description TEXT,
    first_occurrence_date DATE,
    last_reaction_date DATE,
    treatment VARCHAR(500),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_allergen (allergen),
    INDEX idx_allergy_type (allergy_type),
    INDEX idx_severity (severity),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);
```

### patient_insurance Table
```sql
CREATE TABLE patient_insurance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    insurance_provider VARCHAR(100) NOT NULL,
    policy_number VARCHAR(50) NOT NULL,
    group_number VARCHAR(50),
    policy_holder_name VARCHAR(100),
    policy_holder_relationship VARCHAR(50),
    coverage_type ENUM('PRIMARY', 'SECONDARY', 'TERTIARY') DEFAULT 'PRIMARY',
    effective_date DATE,
    expiration_date DATE,
    copay_amount DECIMAL(10,2),
    deductible_amount DECIMAL(10,2),
    coverage_details TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_insurance_provider (insurance_provider),
    INDEX idx_policy_number (policy_number),
    INDEX idx_coverage_type (coverage_type),
    INDEX idx_effective_date (effective_date),
    INDEX idx_is_active (is_active)
);
```

---

## Appointment Service Database (hospital_appointment_db)

### appointments Table
```sql
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    patient_user_id BIGINT NOT NULL,
    doctor_user_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    duration_minutes INT DEFAULT 30,
    status ENUM('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    appointment_type ENUM('CONSULTATION', 'FOLLOW_UP', 'EMERGENCY', 'CHECK_UP', 'PROCEDURE') DEFAULT 'CONSULTATION',
    reason_for_visit TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    cancelled_at TIMESTAMP NULL,
    cancelled_by BIGINT,
    cancellation_reason TEXT,
    
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_patient_user_id (patient_user_id),
    INDEX idx_doctor_user_id (doctor_user_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_appointment_time (appointment_time),
    INDEX idx_status (status),
    INDEX idx_appointment_type (appointment_type),
    INDEX idx_created_at (created_at),
    INDEX idx_cancelled_at (cancelled_at),
    
    CONSTRAINT chk_appointment_date CHECK (appointment_date >= CURDATE()),
    CONSTRAINT chk_duration CHECK (duration_minutes > 0 AND duration_minutes <= 480)
);
```

### doctor_availability Table
```sql
CREATE TABLE doctor_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_user_id BIGINT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_doctor_user_id (doctor_user_id),
    INDEX idx_day_of_week (day_of_week),
    INDEX idx_is_available (is_available),
    
    CONSTRAINT chk_time_order CHECK (start_time < end_time),
    UNIQUE KEY unique_doctor_day (doctor_user_id, day_of_week)
);
```

### appointment_history Table
```sql
CREATE TABLE appointment_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    previous_status ENUM('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW', 'RESCHEDULED'),
    new_status ENUM('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW', 'RESCHEDULED'),
    previous_date DATE,
    new_date DATE,
    previous_time TIME,
    new_time TIME,
    change_reason TEXT,
    changed_by BIGINT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    
    INDEX idx_appointment_id (appointment_id),
    INDEX idx_changed_at (changed_at),
    INDEX idx_changed_by (changed_by)
);
```

---

## Notification Service Database (hospital_notification_db)

### notifications Table
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('EMAIL', 'SMS', 'PUSH', 'IN_APP') DEFAULT 'EMAIL',
    category ENUM('APPOINTMENT_REMINDER', 'APPOINTMENT_CONFIRMATION', 'APPOINTMENT_CANCELLATION', 
                  'SYSTEM_ALERT', 'WELCOME', 'PASSWORD_RESET', 'GENERAL') DEFAULT 'GENERAL',
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    recipient_email VARCHAR(100),
    recipient_phone VARCHAR(15),
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED') DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),
    INDEX idx_sent_at (sent_at)
);
```

### notification_templates Table
```sql
CREATE TABLE notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    category ENUM('APPOINTMENT_REMINDER', 'APPOINTMENT_CONFIRMATION', 'APPOINTMENT_CANCELLATION', 
                  'SYSTEM_ALERT', 'WELCOME', 'PASSWORD_RESET', 'GENERAL') NOT NULL,
    type ENUM('EMAIL', 'SMS', 'PUSH', 'IN_APP') NOT NULL,
    subject VARCHAR(200),
    body_template TEXT NOT NULL,
    variables JSON,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_type (type),
    INDEX idx_is_active (is_active)
);
```

### notification_preferences Table
```sql
CREATE TABLE notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_notifications BOOLEAN DEFAULT TRUE,
    sms_notifications BOOLEAN DEFAULT FALSE,
    push_notifications BOOLEAN DEFAULT TRUE,
    appointment_reminders BOOLEAN DEFAULT TRUE,
    appointment_confirmations BOOLEAN DEFAULT TRUE,
    system_alerts BOOLEAN DEFAULT TRUE,
    marketing_emails BOOLEAN DEFAULT FALSE,
    reminder_hours_before INT DEFAULT 24,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    
    CONSTRAINT chk_reminder_hours CHECK (reminder_hours_before >= 1 AND reminder_hours_before <= 168)
);
```

---

## Cross-Service Data Relationships

### Data References Between Services

#### Auth Service ↔ Patient Service
- `patients.user_id` references `users.id` in auth service
- Foreign key constraint not enforced at database level (microservice pattern)
- Data consistency maintained through application logic and events

#### Auth Service ↔ Appointment Service  
- `appointments.patient_user_id` references `users.id` (patient) in auth service
- `appointments.doctor_user_id` references `users.id` (doctor) in auth service
- `doctor_availability.doctor_user_id` references `users.id` (doctor) in auth service

#### Patient Service ↔ Appointment Service
- `appointments.patient_id` references `patients.id` in patient service
- Data synchronization through Kafka events and API calls

#### All Services ↔ Notification Service
- `notifications.user_id` references `users.id` in auth service
- `notification_preferences.user_id` references `users.id` in auth service

---

## Database Indexes Strategy

### Primary Indexes (Automatically Created)
- All `id` columns (PRIMARY KEY)
- All UNIQUE constraints

### Performance Indexes
- **User lookups**: `username`, `email` in users table
- **Patient searches**: `first_name`, `last_name`, `city` in patients table
- **Appointment queries**: `appointment_date`, `doctor_user_id`, `patient_user_id`
- **Notification processing**: `status`, `created_at`, `priority`
- **Temporal queries**: `created_at`, `updated_at` on all tables

### Composite Indexes
- `(doctor_user_id, day_of_week)` in doctor_availability
- `(patient_id, diagnosis_date)` in medical_history
- `(user_id, category, status)` in notifications

---

## Data Constraints and Validation

### Business Rules Enforced at Database Level

#### Authentication Service
- Username and email must be unique
- Password hash cannot be null
- User must have at least one role

#### Patient Service
- User ID must be unique (one patient per user)
- Email must be unique across all patients
- Date of birth cannot be in the future
- Blood type must be valid enum value

#### Appointment Service
- Appointment date cannot be in the past
- Duration must be between 1 and 480 minutes
- Doctor availability start time must be before end time

#### Notification Service
- Reminder hours must be between 1 and 168 hours
- Retry count cannot exceed max retries

---

## Database Backup and Maintenance

### Backup Strategy
```sql
-- Daily backup script for all databases
mysqldump --single-transaction --routines --triggers hospital_auth_db > auth_backup_$(date +%Y%m%d).sql
mysqldump --single-transaction --routines --triggers hospital_patient_db > patient_backup_$(date +%Y%m%d).sql
mysqldump --single-transaction --routines --triggers hospital_appointment_db > appointment_backup_$(date +%Y%m%d).sql
mysqldump --single-transaction --routines --triggers hospital_notification_db > notification_backup_$(date +%Y%m%d).sql
```

### Maintenance Tasks
```sql
-- Weekly maintenance queries
ANALYZE TABLE users, roles, user_roles;
ANALYZE TABLE patients, medical_history, emergency_contacts;
ANALYZE TABLE appointments, doctor_availability;
ANALYZE TABLE notifications, notification_templates;

-- Cleanup old refresh tokens (monthly)
DELETE FROM refresh_tokens WHERE expiry_date < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Cleanup old password reset tokens (weekly)
DELETE FROM password_reset_tokens WHERE expiry_date < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- Archive old notifications (quarterly)
DELETE FROM notifications WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY) AND status = 'DELIVERED';
```

---

## Performance Considerations

### Query Optimization
- Use appropriate indexes for frequent queries
- Implement pagination for large result sets
- Use EXPLAIN to analyze query performance
- Consider partitioning for large tables (future enhancement)

### Connection Management
- Use connection pooling (HikariCP)
- Set appropriate connection timeouts
- Monitor connection usage and pool size

### Data Growth Management
- Implement data archiving strategies
- Monitor table sizes and index usage
- Plan for horizontal scaling if needed

---

## Security Considerations

### Data Protection
- Passwords stored as BCrypt hashes with salt
- Sensitive medical data protected with access controls
- PII data handling compliant with regulations
- Audit trails for sensitive operations

### Database Security
- Use strong database passwords
- Implement database-level user access controls
- Enable SSL/TLS for database connections
- Regular security audits and updates

---

## Environment-Specific Configurations

### Development Environment
```sql
-- Smaller connection pools
-- Debug logging enabled
-- Relaxed constraints for testing
```

### Production Environment
```sql
-- Optimized connection pools
-- Performance monitoring enabled
-- Strict data validation
-- Automated backups configured
```

### Testing Environment
```sql
-- In-memory databases for unit tests
-- Test data fixtures
-- Faster cleanup procedures
```

---

## Migration Scripts

### Initial Setup
```sql
-- Create databases
CREATE DATABASE hospital_auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE hospital_patient_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE hospital_appointment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE hospital_notification_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create database users
CREATE USER 'hospital_auth'@'localhost' IDENTIFIED BY 'auth_password';
CREATE USER 'hospital_patient'@'localhost' IDENTIFIED BY 'patient_password';
CREATE USER 'hospital_appointment'@'localhost' IDENTIFIED BY 'appointment_password';
CREATE USER 'hospital_notification'@'localhost' IDENTIFIED BY 'notification_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON hospital_auth_db.* TO 'hospital_auth'@'localhost';
GRANT ALL PRIVILEGES ON hospital_patient_db.* TO 'hospital_patient'@'localhost';
GRANT ALL PRIVILEGES ON hospital_appointment_db.* TO 'hospital_appointment'@'localhost';
GRANT ALL PRIVILEGES ON hospital_notification_db.* TO 'hospital_notification'@'localhost';

FLUSH PRIVILEGES;
```

### Version Control
- Use Flyway or Liquibase for database migrations
- Version all schema changes
- Test migrations in development environment first
- Maintain rollback scripts for critical changes

---

## Documentation Maintenance

### Schema Documentation Updates
- Update documentation when schema changes occur
- Include migration notes for major changes
- Document business rules and constraints
- Maintain ER diagrams for complex relationships

### Version History
- **v1.0**: Initial schema design for all services
- **v1.1**: Added patient medical history and emergency contacts
- **v1.2**: Enhanced notification system with preferences
- **v1.3**: Added appointment history tracking

**Last Updated**: January 2024  
**Next Review**: April 2024  
**Database Version**: 1.3
