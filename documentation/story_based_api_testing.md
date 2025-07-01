# Hospital Management System - Story-Based API Testing Guide

## Overview
This guide provides a comprehensive story-based approach to testing all endpoints in the Hospital Management System microservices. The tests follow a realistic user journey from registration to appointment management.

## Prerequisites
- All microservices running (Auth, Patient, Appointment, Notification, API Gateway)
- API Gateway running on port 9000
- Direct service access available on ports 8081-8084
- JWT tokens will be obtained during the authentication flow

---

## 🏥 Story 1: Hospital Administrator Setup Journey

### Scene 1: Admin Registration and Login

**Story**: A new hospital administrator needs to set up the system and create initial user accounts.

#### 1.1 Admin Registration
```bash
# Register the first admin user
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "admin_hospital",
    "email": "admin@hospital.com",
    "password": "Admin123!",
    "firstName": "Hospital",
    "lastName": "Administrator",
    "phoneNumber": "+1234567890",
    "roles": ["admin"]
}'
```

#### 1.2 Admin Login
```bash
# Login to get JWT token
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "admin_hospital",
    "password": "Admin123!"
}'
```

**Save the JWT token from the response for subsequent requests:**
```bash
# Store the token in a variable (replace with actual token)
export ADMIN_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 1.3 Validate Admin Token
```bash
# Validate the JWT token
curl -X POST http://localhost:9000/api/auth/validate \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{admin_jwt}}" \
-d '{
    "token": "'{{admin_jwt}}'"
}'
```

#### 1.4 Get All Users (Admin View)
```bash
# View all users in the system
curl -X GET http://localhost:9000/api/users \
-H "Authorization: Bearer {{admin_jwt}}"
```

---

## 🏥 Story 2: Doctor Onboarding Journey

### Scene 2: Doctor Registration and Profile Setup

**Story**: A new doctor joins the hospital and needs to be registered in the system.

#### 2.1 Register Doctor User
```bash
# Admin creates doctor account
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{admin_jwt}}" \
-d '{
    "username": "dr_smith",
    "email": "dr.smith@hospital.com",
    "password": "Doctor123!",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+1234567891",
    "roles": ["doctor"]
}'
```

#### 2.2 Doctor Login
```bash
# Doctor logs in
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_smith",
    "password": "Doctor123!"
}'
```

**Save the doctor's JWT token:**
```bash
export DOCTOR_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 2.3 Get Doctor Profile
```bash
# Doctor views their profile
curl -X GET http://localhost:9000/api/users/username/dr_smith \
-H "Authorization: Bearer {{doctor_jwt}}"
```

#### 2.4 Set Doctor Availability Schedule
```bash
# Doctor sets their weekly availability
curl -X POST http://localhost:9000/api/appointments/availability \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{doctor_jwt}}" \
-d '{
    "weeklySchedule": [
        {
            "dayOfWeek": "MONDAY",
            "startTime": "09:00:00",
            "endTime": "17:00:00",
            "isAvailable": true
        },
        {
            "dayOfWeek": "TUESDAY",
            "startTime": "09:00:00",
            "endTime": "17:00:00",
            "isAvailable": true
        },
        {
            "dayOfWeek": "WEDNESDAY",
            "startTime": "09:00:00",
            "endTime": "12:00:00",
            "isAvailable": true
        },
        {
            "dayOfWeek": "THURSDAY",
            "startTime": "09:00:00",
            "endTime": "17:00:00",
            "isAvailable": true
        },
        {
            "dayOfWeek": "FRIDAY",
            "startTime": "09:00:00",
            "endTime": "17:00:00",
            "isAvailable": true
        },
        {
            "dayOfWeek": "SATURDAY",
            "isAvailable": false
        },
        {
            "dayOfWeek": "SUNDAY",
            "isAvailable": false
        }
    ]
}'
```

#### 2.5 Check Doctor Availability
```bash
# Check doctor's availability for a specific date
curl -X GET "http://localhost:9000/api/appointments/availability/doctor/2?date=2024-02-15" \
-H "Authorization: Bearer {{doctor_jwt}}"
```

---

## 🏥 Story 3: Patient Registration Journey

### Scene 3: Patient Registration and Profile Creation

**Story**: A new patient wants to register and book appointments.

#### 3.1 Register Patient User
```bash
# Patient self-registration
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe2",
    "email": "john.doe@email.com",
    "password": "Patient123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567892",
    "roles": ["patient"]
}'
```

#### 3.2 Patient Login
```bash
# Patient logs in
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "john_doe2",
    "password": "Patient123!"
}'
```

**Save the patient's JWT token:**
```bash
export PATIENT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3.3 Create Patient Profile
```bash
# Create detailed patient profile
curl -X POST http://localhost:9000/api/patients \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "userId": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com",
    "phoneNumber": "+1234567892",
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
}'
```

#### 3.4 View Patient Profile
```bash
# Patient views their own profile
curl -X GET http://localhost:9000/api/patients/user/3 \
-H "Authorization: Bearer {{patient_jwt}}"
```

#### 3.5 Search for Available Time Slots
```bash
# Patient searches for available appointment slots
curl -X GET "http://localhost:9000/api/appointments/availability/slots?doctorUserId=2&date=2024-02-15&duration=30" \
-H "Authorization: Bearer {{patient_jwt}}"
```

---

## 🏥 Story 4: Appointment Booking Journey

### Scene 4: Patient Books Appointment

**Story**: The patient wants to book an appointment with the doctor.

#### 4.1 Book Appointment
```bash
# Patient books an appointment
curl -X POST http://localhost:9000/api/appointments \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "doctorUserId": 2,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "durationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "reasonForVisit": "Regular checkup and blood pressure monitoring",
    "notes": "Patient reports mild headaches in the morning"
}'
```

#### 4.2 View Patient's Appointments
```bash
# Patient views their appointments
curl -X GET "http://localhost:9000/api/appointments/patient?page=0&size=10" \
-H "Authorization: Bearer {{patient_jwt}}"
```

#### 4.3 Doctor Views Their Appointments
```bash
# Doctor views their appointment schedule
curl -X GET "http://localhost:9000/api/appointments/doctor?date=2024-02-15" \
-H "Authorization: Bearer {{doctor_jwt}}"
```

#### 4.4 Admin Views All Appointments
```bash
# Admin views all appointments in the system
curl -X GET "http://localhost:9000/api/appointments?page=0&size=20" \
-H "Authorization: Bearer {{admin_jwt}}"
```

---

## 🏥 Story 5: Appointment Management Journey

### Scene 5: Appointment Modifications and Status Updates

**Story**: The appointment needs to be managed - confirmed, rescheduled, or cancelled.

#### 5.1 Doctor Confirms Appointment
```bash
# Doctor confirms the appointment
curl -X PUT http://localhost:9000/api/appointments/1/status \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{doctor_jwt}}" \
-d '{
    "status": "CONFIRMED",
    "notes": "Patient confirmed attendance via phone call"
}'
```

#### 5.2 Patient Reschedules Appointment
```bash
# Patient reschedules the appointment
curl -X PUT http://localhost:9000/api/appointments/1/reschedule \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "newAppointmentDate": "2024-02-20",
    "newAppointmentTime": "15:00:00",
    "rescheduleReason": "Patient requested different time due to work schedule"
}'
```

#### 5.3 View Updated Appointment
```bash
# View the updated appointment details
curl -X GET http://localhost:9000/api/appointments/1 \
-H "Authorization: Bearer {{patient_jwt}}"
```

#### 5.4 Cancel Appointment (if needed)
```bash
# Patient cancels the appointment
curl -X DELETE http://localhost:9000/api/appointments/1 \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "cancellationReason": "Patient has a family emergency and cannot attend"
}'
```

---

## 🏥 Story 6: Notification System Journey

### Scene 6: Email Notifications and Reminders

**Story**: The system sends notifications for appointments and other events.

#### 6.1 Send Test Welcome Email
```bash
# Admin sends a test welcome email
curl -X POST "http://localhost:9000/api/test/send-welcome-email?email=john.doe@email.com&name=John Doe&username=john_doe2" \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 6.2 Send Test Appointment Confirmation
```bash
# Send test appointment confirmation email
curl -X POST "http://localhost:9000/api/test/send-appointment-confirmation?email=john.doe@email.com&name=John Doe&date=2024-02-20&time=15:00&doctor=Dr. Jane Smith" \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 6.3 Send Test Appointment Reminder
```bash
# Send test appointment reminder email
curl -X POST "http://localhost:9000/api/test/send-reminder?email=john.doe@email.com&name=John Doe&date=2024-02-20&time=15:00&doctor=Dr. Jane Smith" \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 6.4 Create Notification Record
```bash
# Create a notification record
curl -X POST http://localhost:9000/api/notifications \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{admin_jwt}}" \
-d '{
    "userId": 3,
    "recipientEmail": "john.doe@email.com",
    "recipientName": "John Doe",
    "type": "EMAIL",
    "subject": "Appointment Confirmation",
    "content": "Your appointment has been confirmed for February 20, 2024 at 3:00 PM.",
    "htmlContent": "<html><body><h1>Appointment Confirmed</h1><p>Your appointment has been confirmed for February 20, 2024 at 3:00 PM.</p></body></html>",
    "scheduledAt": "2024-01-15T10:30:00",
    "appointmentId": 1,
    "patientId": 1,
    "doctorId": 1,
    "category": "Appointment Confirmation",
    "templateName": "appointment-confirmation",
    "templateVariables": {
        "patientName": "John Doe",
        "appointmentDate": "February 20, 2024",
        "appointmentTime": "3:00 PM",
        "doctorName": "Dr. Jane Smith"
    }
}'
```

#### 6.5 View Patient Notifications
```bash
# View notifications for the patient
curl -X GET http://localhost:9000/api/notifications/user/3 \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 6.6 Process Scheduled Notifications
```bash
# Manually trigger notification processing
curl -X POST http://localhost:9000/api/notifications/process-scheduled \
-H "Authorization: Bearer {{admin_jwt}}"
```

---

## 🏥 Story 7: Patient Management Journey

### Scene 7: Patient Data Management and Search

**Story**: Staff needs to manage and search patient information.

#### 7.1 Search Patients by Name
```bash
# Search for patients by name
curl -X GET "http://localhost:9000/api/patients/search?name=John" \
-H "Authorization: Bearer {{doctor_jwt}}"
```

#### 7.2 Get Patients by City
```bash
# Get all patients in a specific city
curl -X GET http://localhost:9000/api/patients/city/New%20York \
-H "Authorization: Bearer {{doctor_jwt}}"
```

#### 7.3 Update Patient Profile
```bash
# Update patient information
curl -X PUT http://localhost:9000/api/patients/1 \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "userId": 3,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@email.com",
    "phoneNumber": "+1234567892",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE",
    "address": "456 Oak Avenue",
    "city": "New York",
    "state": "NY",
    "zipCode": "10002",
    "country": "USA",
    "bloodType": "O_POSITIVE",
    "allergies": "Penicillin, Peanuts, Shellfish",
    "medications": "Aspirin 81mg daily, Lisinopril 10mg",
    "medicalConditions": "Hypertension, Seasonal Allergies",
    "insuranceProvider": "Blue Cross Blue Shield",
    "insurancePolicyNumber": "BC12345678"
}'
```

#### 7.4 Get All Patients (Staff View)
```bash
# Staff views all patients
curl -X GET http://localhost:9000/api/patients \
-H "Authorization: Bearer {{doctor_jwt}}"
```

---

## 🏥 Story 8: User Management Journey

### Scene 8: Admin User Management

**Story**: Administrator manages user accounts and roles.

#### 8.1 Get Users by Role
```bash
# Get all doctors in the system
curl -X GET http://localhost:9000/api/users/role/doctor \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 8.2 Get Users by Role (Patients)
```bash
# Get all patients in the system
curl -X GET http://localhost:9000/api/users/role/patient \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 8.3 Update User Profile
```bash
# Admin updates a user profile
curl -X PUT http://localhost:9000/api/users/2 \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{admin_jwt}}" \
-d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "dr.smith@hospital.com",
    "phoneNumber": "+1234567891"
}'
```

#### 8.4 Get User by ID
```bash
# Get specific user by ID
curl -X GET http://localhost:9000/api/users/2 \
-H "Authorization: Bearer {{admin_jwt}}"
```

---

## 🏥 Story 9: System Statistics and Reporting

### Scene 9: Analytics and System Monitoring

**Story**: Staff needs to view system statistics and reports.

#### 9.1 Get Appointment Statistics
```bash
# Get appointment statistics
curl -X GET "http://localhost:9000/api/appointments/statistics?fromDate=2024-01-01&toDate=2024-12-31" \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 9.2 Get Notification Statistics
```bash
# Get notification delivery statistics
curl -X GET "http://localhost:9000/api/notifications/statistics?fromDate=2024-01-01&toDate=2024-12-31" \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 9.3 Retry Failed Notifications
```bash
# Retry sending failed notifications
curl -X POST http://localhost:9000/api/notifications/retry-failed \
-H "Authorization: Bearer {{admin_jwt}}"
```

---

## 🏥 Story 10: Security and Token Management

### Scene 10: Token Refresh and Logout

**Story**: Users need to manage their authentication sessions.

#### 10.1 Refresh JWT Token
```bash
# Refresh the JWT token (use refresh token from login response)
curl -X POST http://localhost:9000/api/auth/refresh \
-H "Content-Type: application/json" \
-d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
}'
```

#### 10.2 User Logout
```bash
# User logs out
curl -X POST http://localhost:9000/api/auth/logout \
-H "Authorization: Bearer {{patient_jwt}}"
```

#### 10.3 Validate Expired Token
```bash
# Test token validation with expired token
curl -X POST http://localhost:9000/api/auth/validate \
-H "Content-Type: application/json" \
-d '{
    "token": "EXPIRED_TOKEN_HERE"
}'
```

---

## 🏥 Story 11: Error Handling and Edge Cases

### Scene 11: Testing Error Scenarios

**Story**: Testing how the system handles various error conditions.

#### 11.1 Try to Access Without Authentication
```bash
# Attempt to access protected endpoint without token
curl -X GET http://localhost:9000/api/patients
```

#### 11.2 Try to Access with Invalid Role
```bash
# Patient tries to access admin-only endpoint
curl -X GET http://localhost:9000/api/users \
-H "Authorization: Bearer {{patient_jwt}}"
```

#### 11.3 Book Appointment with Invalid Data
```bash
# Try to book appointment with past date
curl -X POST http://localhost:9000/api/appointments \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "doctorUserId": 2,
    "appointmentDate": "2023-01-15",
    "appointmentTime": "14:30:00",
    "durationMinutes": 30
}'
```

#### 11.4 Create Patient with Invalid Data
```bash
# Try to create patient with invalid email
curl -X POST http://localhost:9000/api/patients \
-H "Content-Type: application/json" \
-H "Authorization: Bearer {{patient_jwt}}" \
-d '{
    "userId": 4,
    "firstName": "Test",
    "lastName": "User",
    "email": "invalid-email",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE"
}'
```

---

## 🏥 Story 12: Integration Testing

### Scene 12: End-to-End Workflow Testing

**Story**: Testing complete workflows across multiple services.

#### 12.1 Complete Patient Journey
```bash
# 1. Register new patient
curl -X POST http://localhost:9000/api/auth/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "sarah_wilson",
    "email": "sarah.wilson@email.com",
    "password": "Patient123!",
    "firstName": "Sarah",
    "lastName": "Wilson",
    "phoneNumber": "+1234567893",
    "roles": ["patient"]
}'

# 2. Login and get token
curl -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{
    "username": "sarah_wilson",
    "password": "Patient123!"
}'

# 3. Create patient profile
curl -X POST http://localhost:9000/api/patients \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SARAH_TOKEN" \
-d '{
    "userId": 4,
    "firstName": "Sarah",
    "lastName": "Wilson",
    "email": "sarah.wilson@email.com",
    "phoneNumber": "+1234567893",
    "dateOfBirth": "1985-08-20",
    "gender": "FEMALE",
    "address": "789 Pine Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10003",
    "country": "USA",
    "bloodType": "A_POSITIVE",
    "allergies": "None",
    "medications": "None",
    "medicalConditions": "None",
    "insuranceProvider": "Aetna",
    "insurancePolicyNumber": "AE98765432"
}'

# 4. Book appointment
curl -X POST http://localhost:9000/api/appointments \
-H "Content-Type: application/json" \
-H "Authorization: Bearer SARAH_TOKEN" \
-d '{
    "doctorUserId": 2,
    "appointmentDate": "2024-02-25",
    "appointmentTime": "10:00:00",
    "durationMinutes": 45,
    "appointmentType": "CHECK_UP",
    "reasonForVisit": "Annual physical examination",
    "notes": "First time patient, needs comprehensive health assessment"
}'

# 5. View appointment
curl -X GET "http://localhost:9000/api/appointments/patient" \
-H "Authorization: Bearer SARAH_TOKEN"
```

---

## 🏥 Story 13: Performance and Load Testing

### Scene 13: System Performance Testing

#### 13.1 Bulk Patient Creation
```bash
# Create multiple patients in sequence
for i in {1..5}; do
    curl -X POST http://localhost:9000/api/auth/signup \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"patient_$i\",
        \"email\": \"patient$i@email.com\",
        \"password\": \"Patient123!\",
        \"firstName\": \"Patient\",
        \"lastName\": \"$i\",
        \"phoneNumber\": \"+123456789$i\",
        \"roles\": [\"patient\"]
    }"
done
```

#### 13.2 Bulk Appointment Booking
```bash
# Book multiple appointments
for i in {1..3}; do
    curl -X POST http://localhost:9000/api/appointments \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer {{patient_jwt}}" \
    -d "{
        \"doctorUserId\": 2,
        \"appointmentDate\": \"2024-02-2$i\",
        \"appointmentTime\": \"1$i:00:00\",
        \"durationMinutes\": 30,
        \"appointmentType\": \"CONSULTATION\",
        \"reasonForVisit\": \"Regular checkup $i\"
    }"
done
```

---

## 🏥 Story 14: Cleanup and Maintenance

### Scene 14: System Cleanup

#### 14.1 Delete Test Users (Admin Only)
```bash
# Delete test users (use with caution)
curl -X DELETE http://localhost:9000/api/users/4 \
-H "Authorization: Bearer {{admin_jwt}}"
```

#### 14.2 View System Health
```bash
# Check system health endpoints
curl -X GET http://localhost:9000/actuator/health
curl -X GET http://localhost:8081/actuator/health
curl -X GET http://localhost:8082/actuator/health
curl -X GET http://localhost:8083/actuator/health
curl -X GET http://localhost:8084/actuator/health
```

---

## 📋 Testing Checklist

### Authentication & Authorization
- [ ] User registration with different roles
- [ ] User login and JWT token generation
- [ ] Token validation and refresh
- [ ] Role-based access control
- [ ] User logout

### Patient Management
- [ ] Patient profile creation
- [ ] Patient profile updates
- [ ] Patient search functionality
- [ ] Patient data validation
- [ ] Role-based patient access

### Appointment Management
- [ ] Doctor availability setup
- [ ] Appointment booking
- [ ] Appointment rescheduling
- [ ] Appointment cancellation
- [ ] Appointment status updates
- [ ] Time slot availability checking

### Notification System
- [ ] Email notification sending
- [ ] Notification record creation
- [ ] Notification status tracking
- [ ] Failed notification retry
- [ ] Scheduled notification processing

### User Management
- [ ] User profile management
- [ ] User role management
- [ ] User search and filtering
- [ ] User data validation

### Error Handling
- [ ] Invalid authentication
- [ ] Invalid authorization
- [ ] Data validation errors
- [ ] Business logic errors
- [ ] System errors

### Integration Testing
- [ ] End-to-end workflows
- [ ] Service communication
- [ ] Data consistency
- [ ] Event-driven processes

---

## 🚀 Running the Tests

### Prerequisites
1. Start all microservices:
   ```bash
   # Start API Gateway (port 9000)
   # Start Auth Service (port 8081)
   # Start Patient Service (port 8082)
   # Start Appointment Service (port 8083)
   # Start Notification Service (port 8084)
   ```

2. Ensure databases are running and configured

3. Set up environment variables for tokens:
   ```bash
   export ADMIN_TOKEN="your_admin_token"
   export DOCTOR_TOKEN="your_doctor_token"
   export PATIENT_TOKEN="your_patient_token"
   ```

### Execution
1. Run the stories in sequence from 1 to 14
2. Each story builds upon the previous ones
3. Save tokens from authentication responses
4. Verify responses and error handling
5. Check system logs for any issues

### Expected Results
- All endpoints should return appropriate HTTP status codes
- JWT tokens should be valid and contain correct claims
- Role-based access should be enforced
- Data should be consistent across services
- Error messages should be informative
- Notifications should be sent successfully

---

## 📝 Notes

1. **Token Management**: Always save JWT tokens from login responses and use them in subsequent requests
2. **Data Consistency**: Verify that data created in one service is accessible in others
3. **Error Handling**: Test both valid and invalid scenarios
4. **Performance**: Monitor response times and system resources
5. **Security**: Verify that unauthorized access is properly blocked
6. **Logging**: Check service logs for any errors or warnings

This comprehensive testing guide covers all aspects of the Hospital Management System and ensures thorough validation of the microservices architecture.