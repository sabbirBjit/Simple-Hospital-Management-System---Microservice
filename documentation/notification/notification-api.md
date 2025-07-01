# Notification Service API Documentation

## Base URL
```
http://localhost:8084 (Direct)
http://localhost:9000 (Via API Gateway - Recommended)
```

## Overview
The Notification Service handles all system notifications including email notifications, appointment reminders, system alerts, and user communication. This service integrates with all other services through Kafka events and provides comprehensive notification management capabilities including email templates, scheduling, and delivery tracking.

---

## Notification Data Model

### Notification Entity Structure
```json
{
    "id": 1,
    "userId": 123,
    "recipientEmail": "john.doe@example.com",
    "recipientName": "John Doe",
    "type": "EMAIL",
    "subject": "Appointment Confirmation",
    "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
    "htmlContent": "<html><body><h1>Appointment Confirmed</h1><p>Your appointment has been confirmed...</p></body></html>",
    "status": "SENT",
    "errorMessage": null,
    "retryCount": 0,
    "scheduledAt": "2024-01-15T10:30:00",
    "sentAt": "2024-01-15T10:31:00",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:31:00",
    "appointmentId": 456,
    "patientId": 789,
    "doctorId": 101,
    "category": "Appointment Confirmation",
    "templateName": "appointment-confirmation"
}
```

### Notification Type Enum Values
- `EMAIL` - Email notification
- `SMS` - SMS notification (future implementation)
- `PUSH_NOTIFICATION` - Push notification (future implementation)
- `IN_APP` - In-application notification (future implementation)

### Notification Status Enum Values
- `PENDING` - Notification is queued for sending
- `SENT` - Notification has been successfully sent
- `FAILED` - Notification failed to send
- `RETRYING` - Notification is being retried after failure
- `CANCELLED` - Notification has been cancelled

---

## Authentication
Most endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Notification Management Endpoints

### 1. Create Notification
**Endpoint:** `POST /api/notifications`

**Description:** Create a new notification

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Request Body:**
```json
{
    "userId": 123,
    "recipientEmail": "john.doe@example.com",
    "recipientName": "John Doe",
    "type": "EMAIL",
    "subject": "Appointment Confirmation",
    "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
    "htmlContent": "<html><body><h1>Appointment Confirmed</h1><p>Your appointment has been confirmed for February 15, 2024 at 2:30 PM.</p></body></html>",
    "scheduledAt": "2024-01-15T10:30:00",
    "appointmentId": 456,
    "patientId": 789,
    "doctorId": 101,
    "category": "Appointment Confirmation",
    "templateName": "appointment-confirmation",
    "templateVariables": {
        "patientName": "John Doe",
        "appointmentDate": "February 15, 2024",
        "appointmentTime": "2:30 PM",
        "doctorName": "Dr. Jane Smith"
    }
}
```

**Validation Rules:**
- `userId`: Required, must be a valid user ID
- `recipientEmail`: Required, valid email format
- `recipientName`: Optional, max 100 characters
- `type`: Required, must be valid NotificationType enum
- `subject`: Required, max 200 characters
- `content`: Optional, max 5000 characters
- `htmlContent`: Optional, max 10000 characters
- `scheduledAt`: Optional, defaults to current time if not specified
- `category`: Optional, max 50 characters
- `templateName`: Optional, max 100 characters

**Response (Success - 201):**
```json
{
    "success": true,
    "message": "Notification created successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "userId": 123,
        "recipientEmail": "john.doe@example.com",
        "recipientName": "John Doe",
        "type": "EMAIL",
        "subject": "Appointment Confirmation",
        "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
        "status": "PENDING",
        "errorMessage": null,
        "retryCount": 0,
        "scheduledAt": "2024-01-15T10:30:00",
        "sentAt": null,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00",
        "appointmentId": 456,
        "patientId": 789,
        "doctorId": 101,
        "category": "Appointment Confirmation",
        "templateName": "appointment-confirmation"
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
        "userId": "User ID is required",
        "recipientEmail": "Invalid email format",
        "type": "Notification type is required",
        "subject": "Subject is required"
    }
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/notifications \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "userId": 123,
    "recipientEmail": "john.doe@example.com",
    "recipientName": "John Doe",
    "type": "EMAIL",
    "subject": "Appointment Confirmation",
    "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
    "category": "Appointment Confirmation"
}'
```

---

### 2. Get User Notifications
**Endpoint:** `GET /api/notifications/user/{userId}`

**Description:** Get all notifications for a specific user

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT (own notifications only)

**Path Parameters:**
- `userId`: User ID

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10, max: 100)
- `type`: Filter by notification type (optional)
- `status`: Filter by notification status (optional)
- `category`: Filter by notification category (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User notifications retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "userId": 123,
            "recipientEmail": "john.doe@example.com",
            "recipientName": "John Doe",
            "type": "EMAIL",
            "subject": "Appointment Confirmation",
            "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
            "status": "SENT",
            "errorMessage": null,
            "retryCount": 0,
            "scheduledAt": "2024-01-15T10:30:00",
            "sentAt": "2024-01-15T10:31:00",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:31:00",
            "appointmentId": 456,
            "patientId": 789,
            "doctorId": 101,
            "category": "Appointment Confirmation",
            "templateName": "appointment-confirmation"
        }
    ]
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/notifications/user/123?page=0&size=10&type=EMAIL" \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 3. Get User Notifications by Type
**Endpoint:** `GET /api/notifications/user/{userId}/type/{type}`

**Description:** Get notifications for a user filtered by notification type with pagination

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT (own notifications only)

**Path Parameters:**
- `userId`: User ID
- `type`: Notification type (EMAIL, SMS, PUSH_NOTIFICATION, IN_APP)

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10, max: 100)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "User notifications by type retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "content": [
            {
                "id": 1,
                "userId": 123,
                "recipientEmail": "john.doe@example.com",
                "recipientName": "John Doe",
                "type": "EMAIL",
                "subject": "Appointment Confirmation",
                "content": "Your appointment has been confirmed",
                "status": "SENT",
                "scheduledAt": "2024-01-15T10:30:00",
                "sentAt": "2024-01-15T10:31:00",
                "createdAt": "2024-01-15T10:30:00",
                "category": "Appointment Confirmation"
            }
        ],
        "pageable": {
            "pageNumber": 0,
            "pageSize": 10,
            "sort": {
                "empty": false,
                "sorted": true,
                "unsorted": false
            }
        },
        "totalElements": 1,
        "totalPages": 1,
        "last": true,
        "first": true,
        "numberOfElements": 1,
        "empty": false
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/notifications/user/123/type/EMAIL?page=0&size=10" \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 4. Get Appointment Notifications
**Endpoint:** `GET /api/notifications/appointment/{appointmentId}`

**Description:** Get all notifications related to a specific appointment

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Path Parameters:**
- `appointmentId`: Appointment ID

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment notifications retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "userId": 123,
            "recipientEmail": "john.doe@example.com",
            "recipientName": "John Doe",
            "type": "EMAIL",
            "subject": "Appointment Confirmation",
            "content": "Your appointment has been confirmed",
            "status": "SENT",
            "scheduledAt": "2024-01-15T10:30:00",
            "sentAt": "2024-01-15T10:31:00",
            "appointmentId": 456,
            "category": "Appointment Confirmation"
        },
        {
            "id": 2,
            "userId": 123,
            "recipientEmail": "john.doe@example.com",
            "recipientName": "John Doe",
            "type": "EMAIL",
            "subject": "Appointment Reminder",
            "content": "Reminder: You have an appointment tomorrow",
            "status": "SENT",
            "scheduledAt": "2024-01-14T10:30:00",
            "sentAt": "2024-01-14T10:31:00",
            "appointmentId": 456,
            "category": "Appointment Reminder"
        }
    ]
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:9000/api/notifications/appointment/456 \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 5. Get Notification by ID
**Endpoint:** `GET /api/notifications/{id}`

**Description:** Get a specific notification by ID

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT (own notifications only)

**Path Parameters:**
- `id`: Notification ID

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Notification found",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "userId": 123,
        "recipientEmail": "john.doe@example.com",
        "recipientName": "John Doe",
        "type": "EMAIL",
        "subject": "Appointment Confirmation",
        "content": "Your appointment has been confirmed for February 15, 2024 at 2:30 PM.",
        "htmlContent": "<html><body><h1>Appointment Confirmed</h1>...</body></html>",
        "status": "SENT",
        "errorMessage": null,
        "retryCount": 0,
        "scheduledAt": "2024-01-15T10:30:00",
        "sentAt": "2024-01-15T10:31:00",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:31:00",
        "appointmentId": 456,
        "patientId": 789,
        "doctorId": 101,
        "category": "Appointment Confirmation",
        "templateName": "appointment-confirmation"
    }
}
```

**Response (Not Found - 404):**
```json
{
    "success": false,
    "message": "Notification not found with ID: 999",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:9000/api/notifications/1 \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6. Retry Failed Notifications
**Endpoint:** `POST /api/notifications/retry-failed`

**Description:** Manually trigger retry of failed notifications

**Required Roles:** ROLE_ADMIN

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Failed notifications retry initiated",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "retriedNotifications": 5,
        "totalFailedNotifications": 8,
        "retryJobId": "retry-job-12345"
    }
}
```

**Response (Error - 500):**
```json
{
    "success": false,
    "message": "Error retrying failed notifications: Database connection failed",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/notifications/retry-failed \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### 7. Process Scheduled Notifications
**Endpoint:** `POST /api/notifications/process-scheduled`

**Description:** Manually trigger processing of scheduled notifications

**Required Roles:** ROLE_ADMIN

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Scheduled notifications processing initiated",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "processedNotifications": 12,
        "totalScheduledNotifications": 15,
        "processingJobId": "process-job-67890"
    }
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/notifications/process-scheduled \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## Email Testing Endpoints

### 8. Send Test Welcome Email
**Endpoint:** `POST /api/test/send-welcome-email`

**Description:** Send a test welcome email (for testing purposes)

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR (testing only)

**Query Parameters:**
- `email`: Recipient email address (required)
- `name`: Recipient name (required)
- `username`: Username for the welcome email (required)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Welcome email sent successfully to john.doe@example.com",
    "timestamp": "2024-01-15T10:30:00"
}
```

**Response (Error - 400):**
```json
{
    "success": false,
    "message": "Failed to send welcome email",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST "http://localhost:9000/api/test/send-welcome-email?email=john.doe@example.com&name=John Doe&username=john_doe" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### 9. Send Test Appointment Confirmation
**Endpoint:** `POST /api/test/send-appointment-confirmation`

**Description:** Send a test appointment confirmation email

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR (testing only)

**Query Parameters:**
- `email`: Recipient email address (required)
- `name`: Recipient name (required)
- `date`: Appointment date (required)
- `time`: Appointment time (required)
- `doctor`: Doctor name (required)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment confirmation email sent successfully to john.doe@example.com",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST "http://localhost:9000/api/test/send-appointment-confirmation?email=john.doe@example.com&name=John Doe&date=2024-02-15&time=14:30&doctor=Dr. Jane Smith" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### 10. Send Test Appointment Reminder
**Endpoint:** `POST /api/test/send-reminder`

**Description:** Send a test appointment reminder email

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR (testing only)

**Query Parameters:**
- `email`: Recipient email address (required)
- `name`: Recipient name (required)
- `date`: Appointment date (required)
- `time`: Appointment time (required)
- `doctor`: Doctor name (required)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment reminder email sent successfully to john.doe@example.com",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST "http://localhost:9000/api/test/send-reminder?email=john.doe@example.com&name=John Doe&date=2024-02-15&time=14:30&doctor=Dr. Jane Smith" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## Notification Statistics

### 11. Get Notification Statistics
**Endpoint:** `GET /api/notifications/statistics`

**Description:** Get notification delivery statistics and analytics

**Required Roles:** ROLE_ADMIN

**Query Parameters:**
- `fromDate`: Start date for statistics (optional, format: YYYY-MM-DD)
- `toDate`: End date for statistics (optional, format: YYYY-MM-DD)
- `type`: Filter by notification type (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Notification statistics retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "totalNotifications": 1250,
        "statusDistribution": {
            "SENT": 1100,
            "FAILED": 75,
            "PENDING": 50,
            "RETRYING": 20,
            "CANCELLED": 5
        },
        "typeDistribution": {
            "EMAIL": 1200,
            "SMS": 30,
            "PUSH_NOTIFICATION": 15,
            "IN_APP": 5
        },
        "categoryDistribution": {
            "Appointment Confirmation": 400,
            "Appointment Reminder": 350,
            "Welcome": 200,
            "Appointment Cancellation": 150,
            "System Alert": 100,
            "Password Reset": 50
        },
        "deliveryRates": {
            "overallSuccessRate": 88.0,
            "emailSuccessRate": 90.0,
            "averageDeliveryTime": "2.5 seconds",
            "retrySuccessRate": 65.0
        },
        "dailyVolume": [
            {
                "date": "2024-01-10",
                "sent": 45,
                "failed": 3,
                "pending": 2
            },
            {
                "date": "2024-01-11",
                "sent": 52,
                "failed": 4,
                "pending": 1
            }
        ],
        "topFailureReasons": [
            {
                "reason": "Invalid email address",
                "count": 25
            },
            {
                "reason": "SMTP timeout",
                "count": 20
            },
            {
                "reason": "Recipient mailbox full",
                "count": 15
            }
        ]
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/notifications/statistics?fromDate=2024-01-01&toDate=2024-01-31" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## Kafka Event Integration

### Consumed Events

The Notification Service automatically consumes events from other services and creates appropriate notifications:

#### 1. User Created Event
**Topic:** `user.created`
**Trigger:** When a new user is registered in the Auth Service
**Action:** Sends welcome email to new user

**Event Structure:**
```json
{
    "userId": 123,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "roles": ["ROLE_PATIENT"],
    "timestamp": "2024-01-15T10:30:00"
}
```

**Resulting Notification:**
- **Subject:** "Welcome to Hospital Management System"
- **Template:** welcome.html
- **Category:** Welcome
- **Type:** EMAIL

---

#### 2. Patient Registered Event
**Topic:** `patient.registered`
**Trigger:** When a patient profile is created in the Patient Service
**Action:** Sends patient registration confirmation email

**Event Structure:**
```json
{
    "userId": 123,
    "patientId": 456,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "timestamp": "2024-01-15T10:30:00"
}
```

**Resulting Notification:**
- **Subject:** "Patient Profile Created"
- **Content:** "Your patient profile has been successfully created in our system."
- **Category:** Patient Registration
- **Type:** EMAIL

---

#### 3. Appointment Booked Event
**Topic:** `appointment.booked`
**Trigger:** When an appointment is booked in the Appointment Service
**Action:** Sends appointment confirmation email

**Event Structure:**
```json
{
    "appointmentId": 789,
    "patientUserId": 123,
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "type": "CONSULTATION",
    "timestamp": "2024-01-15T10:30:00"
}
```

**Resulting Notification:**
- **Subject:** "Appointment Confirmation"
- **Template:** appointment-confirmation.html
- **Category:** Appointment Confirmation
- **Type:** EMAIL

---

#### 4. Appointment Cancelled Event
**Topic:** `appointment.cancelled`
**Trigger:** When an appointment is cancelled
**Action:** Sends appointment cancellation notification

**Event Structure:**
```json
{
    "appointmentId": 789,
    "patientUserId": 123,
    "doctorUserId": 456,
    "cancellationReason": "Patient emergency",
    "cancelledBy": 123,
    "cancelledAt": "2024-01-15T10:35:00",
    "timestamp": "2024-01-15T10:35:00"
}
```

**Resulting Notification:**
- **Subject:** "Appointment Cancelled"
- **Content:** "Your appointment has been cancelled. Reason: Patient emergency"
- **Category:** Appointment Cancellation
- **Type:** EMAIL

---

#### 5. Appointment Reminder Event
**Topic:** `appointment.reminder`
**Trigger:** Scheduled job 24 hours before appointment
**Action:** Sends appointment reminder email

**Event Structure:**
```json
{
    "appointmentId": 789,
    "patientUserId": 123,
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "reminderType": "24_HOUR",
    "timestamp": "2024-01-14T14:30:00"
}
```

**Resulting Notification:**
- **Subject:** "Appointment Reminder"
- **Template:** appointment-reminder.html
- **Category:** Appointment Reminder
- **Type:** EMAIL
- **Scheduled:** For immediate delivery

---

## Email Templates

### Built-in Email Templates

#### 1. Welcome Email Template
**Template Name:** `welcome.html`
**Usage:** New user registration
**Variables:** `recipientName`, `username`

**HTML Structure:**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Welcome to Hospital Management System</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
        <h1 style="color: #2c3e50;">Welcome to Hospital Management System</h1>
        <p>Dear {{recipientName}},</p>
        <p>Welcome to our Hospital Management System! Your account has been successfully created.</p>
        <p><strong>Username:</strong> {{username}}</p>
        <p>You can now log in to access your account and manage your medical information.</p>
        <p>Best regards,<br>Hospital Management Team</p>
    </div>
</body>
</html>
```

#### 2. Appointment Confirmation Template
**Template Name:** `appointment-confirmation.html`
**Usage:** Appointment booking confirmation
**Variables:** `recipientName`, `appointmentDate`, `appointmentTime`, `doctorName`

**HTML Structure:**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Appointment Confirmation</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
        <h1 style="color: #27ae60;">Appointment Confirmed</h1>
        <p>Dear {{recipientName}},</p>
        <p>Your appointment has been successfully confirmed:</p>
        <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px;">
            <p><strong>Date:</strong> {{appointmentDate}}</p>
            <p><strong>Time:</strong> {{appointmentTime}}</p>
            <p><strong>Doctor:</strong> {{doctorName}}</p>
        </div>
        <p>Please arrive 15 minutes before your scheduled appointment time.</p>
        <p>Best regards,<br>Hospital Management Team</p>
    </div>
</body>
</html>
```

#### 3. Appointment Reminder Template
**Template Name:** `appointment-reminder.html`
**Usage:** Appointment reminder 24 hours before
**Variables:** `recipientName`, `appointmentDate`, `appointmentTime`, `doctorName`

**HTML Structure:**
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Appointment Reminder</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
        <h1 style="color: #f39c12;">Appointment Reminder</h1>
        <p>Dear {{recipientName}},</p>
        <p>This is a friendly reminder about your upcoming appointment:</p>
        <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #f39c12;">
            <p><strong>Date:</strong> {{appointmentDate}}</p>
            <p><strong>Time:</strong> {{appointmentTime}}</p>
            <p><strong>Doctor:</strong> {{doctorName}}</p>
        </div>
        <p>Please arrive 15 minutes before your scheduled appointment time.</p>
        <p>Best regards,<br>Hospital Management Team</p>
    </div>
</body>
</html>
```

---

## Scheduled Jobs

### Automatic Notification Processing

#### 1. Scheduled Notifications Processor
**Schedule:** Every 5 minutes
**Purpose:** Process notifications scheduled for sending
**Job:** `NotificationSchedulerService.processScheduledNotifications()`

#### 2. Failed Notifications Retry
**Schedule:** Every 15 minutes
**Purpose:** Retry failed notifications with retry count < 3
**Job:** `NotificationSchedulerService.retryFailedNotifications()`

#### 3. Notification Cleanup
**Schedule:** Daily at midnight
**Purpose:** Archive old notifications and clean up delivered notifications
**Retention:** 90 days for delivered notifications, 30 days for failed notifications

---

## Error Handling

### Enhanced Error Responses

**Validation Error (400):**
```json
{
    "success": false,
    "message": "Validation failed",
    "timestamp": "2024-01-15T10:30:00",
    "errors": {
        "userId": "User ID is required",
        "recipientEmail": "Invalid email format",
        "type": "Notification type is required",
        "subject": "Subject is required"
    },
    "path": "/api/notifications",
    "correlationId": "abc123-def456-ghi789"
}
```

**Email Sending Error (500):**
```json
{
    "success": false,
    "message": "Failed to send email notification",
    "timestamp": "2024-01-15T10:30:00",
    "error": {
        "type": "SMTP_ERROR",
        "details": "Connection timeout to SMTP server",
        "retryable": true
    },
    "path": "/api/notifications",
    "correlationId": "abc123-def456-ghi789"
}
```

**Authorization Error (403):**
```json
{
    "error": "Access Denied: Insufficient permissions to access notifications",
    "status": 403,
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/notifications/user/123",
    "correlationId": "abc123-def456-ghi789"
}
```

**Service Unavailable (503):**
```json
{
    "success": false,
    "message": "Email service temporarily unavailable",
    "timestamp": "2024-01-15T10:30:00",
    "error": {
        "type": "SERVICE_UNAVAILABLE",
        "details": "SMTP server maintenance in progress",
        "estimatedRecovery": "2024-01-15T12:00:00"
    }
}
```

---

## Security Considerations

### Access Control Matrix

| Operation | Admin | Doctor | Nurse | Patient | Notes |
|-----------|-------|--------|-------|---------|-------|
| Create Notification | ✅ | ✅ | ✅ | ❌ | Medical staff can send notifications |
| View All Notifications | ✅ | ❌ | ❌ | ❌ | Admin only for system overview |
| View User Notifications | ✅ | ✅ | ✅ | Own Only | Users can view their own notifications |
| View Appointment Notifications | ✅ | ✅ | ✅ | ❌ | Medical staff for appointment-related |
| Retry Failed Notifications | ✅ | ❌ | ❌ | ❌ | Admin only for system maintenance |
| Process Scheduled | ✅ | ❌ | ❌ | ❌ | Admin only for system operations |
| View Statistics | ✅ | ❌ | ❌ | ❌ | Admin only for system analytics |
| Send Test Emails | ✅ | ✅ | ❌ | ❌ | Testing and troubleshooting |

### Data Protection
- **Email Content**: All email content is sanitized to prevent XSS attacks
- **Personal Information**: PII is handled according to privacy regulations
- **Audit Logging**: All notification activities are logged for compliance
- **Rate Limiting**: Email sending is rate-limited to prevent abuse
- **Template Security**: Email templates are validated and sanitized

---

## Configuration

### Email Configuration (Mailtrap Sandbox)

```properties
# SMTP Configuration for Mailtrap
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Mailtrap Specific Configuration
mailtrap.api.token=YOUR_MAILTRAP_API_TOKEN
mailtrap.sandbox.enabled=true
mailtrap.inbox.id=YOUR_INBOX_ID
mailtrap.from.email=noreply@hospital.com
mailtrap.from.name=Hospital Management System
```

### Notification Configuration

```properties
# Notification Settings
notification.email.from=noreply@hospital.com
notification.email.templates.appointment-reminder=appointment-reminder.html
notification.email.templates.appointment-confirmation=appointment-confirmation.html
notification.email.templates.welcome=welcome.html

# Retry Configuration
notification.retry.max-attempts=3
notification.retry.delay-minutes=5
notification.cleanup.retention-days=90
```

---

## Performance Considerations

### Database Optimization
- Indexed fields: `user_id`, `status`, `created_at`, `scheduled_at`
- Partition by date for large notification volumes
- Archive old notifications automatically

### Email Delivery Optimization
- **Async Processing**: All email sending is asynchronous
- **Batch Processing**: Multiple notifications processed in batches
- **Connection Pooling**: SMTP connection pooling for efficiency
- **Retry Strategy**: Exponential backoff for failed notifications

### Monitoring
- **Delivery Rates**: Track successful vs failed delivery rates
- **Response Times**: Monitor email sending performance
- **Queue Depth**: Monitor notification queue depth
- **Error Rates**: Track and alert on high error rates

---

## Integration Testing

### Complete Notification Flow Test
```bash
#!/bin/bash

# 1. Test welcome email
curl -X POST "http://localhost:9000/api/test/send-welcome-email?email=test@example.com&name=Test User&username=test_user" \
-H "Authorization: Bearer ADMIN_TOKEN"

# 2. Create manual notification
NOTIFICATION_RESPONSE=$(curl -s -X POST http://localhost:9000/api/notifications \
-H "Authorization: Bearer ADMIN_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "userId": 123,
    "recipientEmail": "test@example.com",
    "recipientName": "Test User",
    "type": "EMAIL",
    "subject": "Test Notification",
    "content": "This is a test notification",
    "category": "Test"
}')

NOTIFICATION_ID=$(echo $NOTIFICATION_RESPONSE | jq -r '.data.id')

# 3. Check notification status
curl -X GET http://localhost:9000/api/notifications/$NOTIFICATION_ID \
-H "Authorization: Bearer ADMIN_TOKEN"

# 4. Get user notifications
curl -X GET http://localhost:9000/api/notifications/user/123 \
-H "Authorization: Bearer ADMIN_TOKEN"

# 5. Test appointment confirmation
curl -X POST "http://localhost:9000/api/test/send-appointment-confirmation?email=test@example.com&name=Test User&date=2024-02-15&time=14:30&doctor=Dr. Test" \
-H "Authorization: Bearer ADMIN_TOKEN"

# 6. Get notification statistics
curl -X GET http://localhost:9000/api/notifications/statistics \
-H "Authorization: Bearer ADMIN_TOKEN"

# 7. Retry failed notifications
curl -X POST http://localhost:9000/api/notifications/retry-failed \
-H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Troubleshooting

### Common Issues

#### Email Not Sending
```
Error: "Failed to send email notification"
Solution: 
1. Check SMTP configuration
2. Verify Mailtrap credentials
3. Check network connectivity
4. Review notification service logs
```

#### High Retry Count
```
Error: "Notification exceeded maximum retry attempts"
Solution:
1. Check recipient email validity
2. Verify SMTP server status
3. Review error messages in notification logs
4. Check rate limiting settings
```

#### Template Not Found
```
Error: "Email template not found"
Solution:
1. Verify template name in notification request
2. Check template file exists in resources
3. Verify template configuration
```

### Debug Endpoints
```bash
# Check service health
curl http://localhost:8084/actuator/health

# Check notification queue depth
curl http://localhost:9000/api/notifications/statistics

# Test email connectivity
curl -X POST "http://localhost:9000/api/test/send-welcome-email?email=test@example.com&name=Test&username=test"
```

---

## Status Codes Summary

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 201 | Notification Created Successfully |
| 400 | Bad Request (Validation Error) |
| 401 | Unauthorized (Missing/Invalid JWT) |
| 403 | Forbidden (Insufficient Permissions) |
| 404 | Not Found (Notification Not Found) |
| 500 | Internal Server Error (Email Sending Failed) |
| 503 | Service Unavailable (Email Service Down) |

---

## Future Enhancements

### Planned Features
1. **SMS Notifications**: Integration with SMS gateway providers
2. **Push Notifications**: Mobile push notification support
3. **In-App Notifications**: Real-time in-application notifications
4. **Template Editor**: Web-based email template editor
5. **A/B Testing**: Email template A/B testing capabilities
6. **Analytics Dashboard**: Comprehensive notification analytics
7. **Webhook Support**: Webhook notifications for external systems

### Technical Improvements
1. **Redis Queue**: Redis-based notification queue for better performance
2. **Email Tracking**: Email open and click tracking
3. **Delivery Receipts**: SMTP delivery receipt handling
4. **Multi-tenant**: Support for multiple organization templates
5. **Internationalization**: Multi-language email templates
