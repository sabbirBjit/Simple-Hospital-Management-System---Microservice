# Appointment Management Service API Documentation

## Base URL
```
http://localhost:8083 (Direct)
http://localhost:9000 (Via API Gateway - Recommended)
```

## Overview
The Appointment Management Service handles all appointment-related operations including appointment booking, rescheduling, cancellation, doctor availability management, and appointment tracking. This service integrates with Patient Service and Auth Service to provide comprehensive appointment management capabilities.

---

## Appointment Data Model

### Appointment Entity Structure
```json
{
    "id": 1,
    "patientId": 10,
    "doctorId": 5,
    "patientUserId": 123,
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "durationMinutes": 30,
    "status": "SCHEDULED",
    "appointmentType": "CONSULTATION",
    "reasonForVisit": "Regular checkup and blood pressure monitoring",
    "notes": "Patient reports mild headaches in the morning",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "createdBy": 123,
    "cancelledAt": null,
    "cancelledBy": null,
    "cancellationReason": null,
    "appointmentDateTime": "2024-02-15T14:30:00",
    "appointmentEndTime": "2024-02-15T15:00:00",
    "statusDisplay": "Scheduled",
    "typeDisplay": "Consultation",
    "patientName": "John Doe",
    "patientEmail": "john.doe@example.com",
    "doctorName": "Dr. Jane Smith",
    "doctorEmail": "dr.smith@hospital.com"
}
```

### Appointment Status Enum Values
- `SCHEDULED` - Appointment is scheduled and confirmed
- `CONFIRMED` - Appointment confirmed by both parties
- `CANCELLED` - Appointment has been cancelled
- `COMPLETED` - Appointment has been completed
- `NO_SHOW` - Patient did not show up for appointment
- `RESCHEDULED` - Appointment has been rescheduled

### Appointment Type Enum Values
- `CONSULTATION` - General consultation
- `FOLLOW_UP` - Follow-up appointment
- `EMERGENCY` - Emergency appointment
- `CHECK_UP` - Routine check-up
- `PROCEDURE` - Medical procedure

---

## Authentication
All endpoints require JWT authentication via the `Authorization` header:
```
Authorization: Bearer <JWT_TOKEN>
```

## Appointment Management Endpoints

### 1. Book Appointment
**Endpoint:** `POST /api/appointments`

**Description:** Book a new appointment with a doctor

**Required Roles:** ROLE_PATIENT, ROLE_ADMIN, ROLE_DOCTOR (for patients)

**Request Body:**
```json
{
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "durationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "reasonForVisit": "Regular checkup and blood pressure monitoring",
    "notes": "Patient reports mild headaches in the morning"
}
```

**Validation Rules:**
- `doctorUserId`: Required, must be a valid doctor user ID
- `appointmentDate`: Required, must be a future date, cannot be more than 30 days in advance
- `appointmentTime`: Required, must be within doctor's availability hours
- `durationMinutes`: Optional, default 30, min 15, max 480 minutes
- `appointmentType`: Optional, default CONSULTATION
- `reasonForVisit`: Optional, max 1000 characters
- `notes`: Optional, max 2000 characters

**Business Logic Validation:**
- Doctor must be available on the requested date/time
- No conflicting appointments for the doctor
- Patient cannot book more than 3 appointments per month
- Appointment must be during doctor's working hours
- Minimum 2 hours advance booking required

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment booked successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "patientId": 10,
        "doctorId": 5,
        "patientUserId": 123,
        "doctorUserId": 456,
        "appointmentDate": "2024-02-15",
        "appointmentTime": "14:30:00",
        "durationMinutes": 30,
        "status": "SCHEDULED",
        "appointmentType": "CONSULTATION",
        "reasonForVisit": "Regular checkup and blood pressure monitoring",
        "notes": "Patient reports mild headaches in the morning",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00",
        "createdBy": 123,
        "appointmentDateTime": "2024-02-15T14:30:00",
        "appointmentEndTime": "2024-02-15T15:00:00",
        "statusDisplay": "Scheduled",
        "typeDisplay": "Consultation"
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
        "doctorUserId": "Doctor User ID is required",
        "appointmentDate": "Appointment date must be in the future",
        "appointmentTime": "Appointment time is required"
    }
}
```

**Response (Business Logic Error - 400):**
```json
{
    "success": false,
    "message": "Doctor is not available at the requested time",
    "timestamp": "2024-01-15T10:30:00"
}
```

**Response (Conflict Error - 409):**
```json
{
    "success": false,
    "message": "Time slot conflicts with existing appointment",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/appointments \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "durationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "reasonForVisit": "Regular checkup and blood pressure monitoring",
    "notes": "Patient reports mild headaches in the morning"
}'
```

---

### 2. Get Patient Appointments
**Endpoint:** `GET /api/appointments/patient`

**Description:** Get all appointments for the authenticated patient

**Required Roles:** ROLE_PATIENT

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `status`: Filter by appointment status (optional)
- `fromDate`: Filter appointments from date (optional, format: YYYY-MM-DD)
- `toDate`: Filter appointments to date (optional, format: YYYY-MM-DD)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Patient appointments retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "patientId": 10,
            "doctorId": 5,
            "patientUserId": 123,
            "doctorUserId": 456,
            "appointmentDate": "2024-02-15",
            "appointmentTime": "14:30:00",
            "durationMinutes": 30,
            "status": "SCHEDULED",
            "appointmentType": "CONSULTATION",
            "reasonForVisit": "Regular checkup",
            "notes": "Patient reports mild headaches",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:30:00",
            "appointmentDateTime": "2024-02-15T14:30:00",
            "appointmentEndTime": "2024-02-15T15:00:00",
            "statusDisplay": "Scheduled",
            "typeDisplay": "Consultation",
            "doctorName": "Dr. Jane Smith",
            "doctorEmail": "dr.smith@hospital.com"
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
curl -X GET "http://localhost:9000/api/appointments/patient?page=0&size=10&status=SCHEDULED" \
-H "Authorization: Bearer PATIENT_JWT_TOKEN"
```

---

### 3. Get Doctor Appointments
**Endpoint:** `GET /api/appointments/doctor`

**Description:** Get all appointments for the authenticated doctor

**Required Roles:** ROLE_DOCTOR

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `date`: Filter by specific date (optional, format: YYYY-MM-DD)
- `status`: Filter by appointment status (optional)
- `fromDate`: Filter appointments from date (optional)
- `toDate`: Filter appointments to date (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Doctor appointments retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "patientId": 10,
            "doctorId": 5,
            "patientUserId": 123,
            "doctorUserId": 456,
            "appointmentDate": "2024-02-15",
            "appointmentTime": "14:30:00",
            "durationMinutes": 30,
            "status": "SCHEDULED",
            "appointmentType": "CONSULTATION",
            "reasonForVisit": "Regular checkup",
            "notes": "Patient reports mild headaches",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:30:00",
            "appointmentDateTime": "2024-02-15T14:30:00",
            "appointmentEndTime": "2024-02-15T15:00:00",
            "statusDisplay": "Scheduled",
            "typeDisplay": "Consultation",
            "patientName": "John Doe",
            "patientEmail": "john.doe@example.com"
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
curl -X GET "http://localhost:9000/api/appointments/doctor?date=2024-02-15" \
-H "Authorization: Bearer DOCTOR_JWT_TOKEN"
```

---

### 4. Get All Appointments (Admin/Staff)
**Endpoint:** `GET /api/appointments`

**Description:** Get all appointments in the system (Admin, Doctor, Nurse access)

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20, max: 100)
- `patientUserId`: Filter by patient user ID (optional)
- `doctorUserId`: Filter by doctor user ID (optional)
- `status`: Filter by appointment status (optional)
- `date`: Filter by specific date (optional)
- `fromDate`: Filter appointments from date (optional)
- `toDate`: Filter appointments to date (optional)
- `appointmentType`: Filter by appointment type (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointments retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "patientId": 10,
            "doctorId": 5,
            "patientUserId": 123,
            "doctorUserId": 456,
            "appointmentDate": "2024-02-15",
            "appointmentTime": "14:30:00",
            "durationMinutes": 30,
            "status": "SCHEDULED",
            "appointmentType": "CONSULTATION",
            "reasonForVisit": "Regular checkup",
            "notes": "Patient reports mild headaches",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:30:00",
            "appointmentDateTime": "2024-02-15T14:30:00",
            "appointmentEndTime": "2024-02-15T15:00:00",
            "statusDisplay": "Scheduled",
            "typeDisplay": "Consultation",
            "patientName": "John Doe",
            "patientEmail": "john.doe@example.com",
            "doctorName": "Dr. Jane Smith",
            "doctorEmail": "dr.smith@hospital.com"
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
curl -X GET "http://localhost:9000/api/appointments?doctorUserId=456&status=SCHEDULED" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### 5. Get Appointment by ID
**Endpoint:** `GET /api/appointments/{id}`

**Description:** Get specific appointment by ID

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT (own appointments only)

**Path Parameters:**
- `id`: Appointment ID

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment found",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "patientId": 10,
        "doctorId": 5,
        "patientUserId": 123,
        "doctorUserId": 456,
        "appointmentDate": "2024-02-15",
        "appointmentTime": "14:30:00",
        "durationMinutes": 30,
        "status": "SCHEDULED",
        "appointmentType": "CONSULTATION",
        "reasonForVisit": "Regular checkup and blood pressure monitoring",
        "notes": "Patient reports mild headaches in the morning",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00",
        "appointmentDateTime": "2024-02-15T14:30:00",
        "appointmentEndTime": "2024-02-15T15:00:00",
        "statusDisplay": "Scheduled",
        "typeDisplay": "Consultation",
        "patientName": "John Doe",
        "patientEmail": "john.doe@example.com",
        "doctorName": "Dr. Jane Smith",
        "doctorEmail": "dr.smith@hospital.com"
    }
}
```

**Response (Not Found - 404):**
```json
{
    "success": false,
    "message": "Appointment not found with ID: 999",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X GET http://localhost:9000/api/appointments/1 \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 6. Cancel Appointment
**Endpoint:** `DELETE /api/appointments/{id}`

**Description:** Cancel an existing appointment

**Required Roles:** ROLE_PATIENT (own appointments), ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Path Parameters:**
- `id`: Appointment ID

**Request Body:**
```json
{
    "cancellationReason": "Patient has a family emergency and cannot attend"
}
```

**Validation Rules:**
- `cancellationReason`: Optional, max 500 characters
- Appointment must be in SCHEDULED or CONFIRMED status
- Cancellation must be at least 2 hours before appointment time

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment cancelled successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "patientId": 10,
        "doctorId": 5,
        "patientUserId": 123,
        "doctorUserId": 456,
        "appointmentDate": "2024-02-15",
        "appointmentTime": "14:30:00",
        "durationMinutes": 30,
        "status": "CANCELLED",
        "appointmentType": "CONSULTATION",
        "reasonForVisit": "Regular checkup",
        "notes": "Patient reports mild headaches",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:35:00",
        "cancelledAt": "2024-01-15T10:35:00",
        "cancelledBy": 123,
        "cancellationReason": "Patient has a family emergency and cannot attend",
        "appointmentDateTime": "2024-02-15T14:30:00",
        "appointmentEndTime": "2024-02-15T15:00:00",
        "statusDisplay": "Cancelled",
        "typeDisplay": "Consultation"
    }
}
```

**Response (Business Logic Error - 400):**
```json
{
    "success": false,
    "message": "Appointment cannot be cancelled in current status",
    "timestamp": "2024-01-15T10:30:00"
}
```

**cURL Command:**
```bash
curl -X DELETE http://localhost:9000/api/appointments/1 \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "cancellationReason": "Patient has a family emergency and cannot attend"
}'
```

---

### 7. Reschedule Appointment
**Endpoint:** `PUT /api/appointments/{id}/reschedule`

**Description:** Reschedule an existing appointment to a new date/time

**Required Roles:** ROLE_PATIENT (own appointments), ROLE_ADMIN, ROLE_DOCTOR

**Path Parameters:**
- `id`: Appointment ID

**Request Body:**
```json
{
    "newAppointmentDate": "2024-02-20",
    "newAppointmentTime": "15:00:00",
    "rescheduleReason": "Patient requested different time due to work schedule"
}
```

**Validation Rules:**
- `newAppointmentDate`: Required, must be a future date
- `newAppointmentTime`: Required, must be within doctor's availability
- `rescheduleReason`: Optional, max 500 characters
- Original appointment must be in SCHEDULED or CONFIRMED status
- New time slot must be available

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment rescheduled successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "patientId": 10,
        "doctorId": 5,
        "patientUserId": 123,
        "doctorUserId": 456,
        "appointmentDate": "2024-02-20",
        "appointmentTime": "15:00:00",
        "durationMinutes": 30,
        "status": "RESCHEDULED",
        "appointmentType": "CONSULTATION",
        "reasonForVisit": "Regular checkup",
        "notes": "Patient reports mild headaches. Rescheduled due to work schedule.",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:40:00",
        "appointmentDateTime": "2024-02-20T15:00:00",
        "appointmentEndTime": "2024-02-20T15:30:00",
        "statusDisplay": "Rescheduled",
        "typeDisplay": "Consultation"
    }
}
```

**cURL Command:**
```bash
curl -X PUT http://localhost:9000/api/appointments/1/reschedule \
-H "Authorization: Bearer YOUR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "newAppointmentDate": "2024-02-20",
    "newAppointmentTime": "15:00:00",
    "rescheduleReason": "Patient requested different time due to work schedule"
}'
```

---

### 8. Update Appointment Status
**Endpoint:** `PUT /api/appointments/{id}/status`

**Description:** Update appointment status (Doctor/Admin only)

**Required Roles:** ROLE_DOCTOR, ROLE_ADMIN

**Path Parameters:**
- `id`: Appointment ID

**Request Body:**
```json
{
    "status": "CONFIRMED",
    "notes": "Patient confirmed attendance via phone call"
}
```

**Validation Rules:**
- `status`: Required, must be valid AppointmentStatus enum
- `notes`: Optional, max 500 characters
- Status transition must be valid (e.g., SCHEDULED -> CONFIRMED)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment status updated successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "id": 1,
        "patientId": 10,
        "doctorId": 5,
        "patientUserId": 123,
        "doctorUserId": 456,
        "appointmentDate": "2024-02-15",
        "appointmentTime": "14:30:00",
        "durationMinutes": 30,
        "status": "CONFIRMED",
        "appointmentType": "CONSULTATION",
        "reasonForVisit": "Regular checkup",
        "notes": "Patient reports mild headaches. Patient confirmed attendance via phone call",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:45:00",
        "appointmentDateTime": "2024-02-15T14:30:00",
        "appointmentEndTime": "2024-02-15T15:00:00",
        "statusDisplay": "Confirmed",
        "typeDisplay": "Consultation"
    }
}
```

**cURL Command:**
```bash
curl -X PUT http://localhost:9000/api/appointments/1/status \
-H "Authorization: Bearer DOCTOR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "status": "CONFIRMED",
    "notes": "Patient confirmed attendance via phone call"
}'
```

---

## Doctor Availability Management

### 9. Get Doctor Availability
**Endpoint:** `GET /api/appointments/availability/doctor/{doctorUserId}`

**Description:** Get doctor's availability schedule

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, ROLE_PATIENT

**Path Parameters:**
- `doctorUserId`: Doctor's user ID

**Query Parameters:**
- `date`: Specific date to check availability (optional, format: YYYY-MM-DD)
- `week`: Week to check availability (optional, format: YYYY-MM-DD for week start)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Doctor availability retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "doctorUserId": 456,
        "doctorName": "Dr. Jane Smith",
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
                "startTime": null,
                "endTime": null,
                "isAvailable": false
            },
            {
                "dayOfWeek": "SUNDAY",
                "startTime": null,
                "endTime": null,
                "isAvailable": false
            }
        ],
        "availableTimeSlots": [
            {
                "date": "2024-02-15",
                "timeSlots": [
                    "09:00:00",
                    "09:30:00",
                    "10:00:00",
                    "10:30:00",
                    "11:00:00",
                    "11:30:00",
                    "14:00:00",
                    "15:00:00",
                    "15:30:00",
                    "16:00:00",
                    "16:30:00"
                ]
            }
        ]
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/appointments/availability/doctor/456?date=2024-02-15" \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### 10. Set Doctor Availability
**Endpoint:** `POST /api/appointments/availability`

**Description:** Set doctor's weekly availability schedule (Doctor/Admin only)

**Required Roles:** ROLE_DOCTOR (own schedule), ROLE_ADMIN

**Request Body:**
```json
{
    "doctorUserId": 456,
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
}
```

**Validation Rules:**
- `doctorUserId`: Required, must be valid doctor user ID
- `dayOfWeek`: Required, must be valid day enum
- `startTime`: Required if isAvailable is true
- `endTime`: Required if isAvailable is true, must be after startTime
- `isAvailable`: Required boolean

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Doctor availability updated successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "doctorUserId": 456,
        "weeklySchedule": [
            {
                "id": 1,
                "dayOfWeek": "MONDAY",
                "startTime": "09:00:00",
                "endTime": "17:00:00",
                "isAvailable": true,
                "createdAt": "2024-01-15T10:30:00",
                "updatedAt": "2024-01-15T10:30:00"
            }
        ]
    }
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:9000/api/appointments/availability \
-H "Authorization: Bearer DOCTOR_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "doctorUserId": 456,
    "weeklySchedule": [
        {
            "dayOfWeek": "MONDAY",
            "startTime": "09:00:00",
            "endTime": "17:00:00",
            "isAvailable": true
        }
    ]
}'
```

---

### 11. Get Available Time Slots
**Endpoint:** `GET /api/appointments/availability/slots`

**Description:** Get available time slots for a specific doctor and date

**Required Roles:** ROLE_PATIENT, ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Query Parameters:**
- `doctorUserId`: Doctor's user ID (required)
- `date`: Date to check availability (required, format: YYYY-MM-DD)
- `duration`: Appointment duration in minutes (optional, default: 30)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Available time slots retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "doctorUserId": 456,
        "doctorName": "Dr. Jane Smith",
        "date": "2024-02-15",
        "duration": 30,
        "availableSlots": [
            {
                "startTime": "09:00:00",
                "endTime": "09:30:00",
                "available": true
            },
            {
                "startTime": "09:30:00",
                "endTime": "10:00:00",
                "available": true
            },
            {
                "startTime": "10:00:00",
                "endTime": "10:30:00",
                "available": true
            },
            {
                "startTime": "14:00:00",
                "endTime": "14:30:00",
                "available": true
            },
            {
                "startTime": "15:00:00",
                "endTime": "15:30:00",
                "available": true
            }
        ],
        "totalAvailableSlots": 5,
        "workingHours": {
            "startTime": "09:00:00",
            "endTime": "17:00:00"
        }
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/appointments/availability/slots?doctorUserId=456&date=2024-02-15&duration=30" \
-H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Statistics and Reporting

### 12. Get Appointment Statistics
**Endpoint:** `GET /api/appointments/statistics`

**Description:** Get appointment statistics and analytics (Admin, Doctor only)

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR

**Query Parameters:**
- `fromDate`: Start date for statistics (optional, format: YYYY-MM-DD)
- `toDate`: End date for statistics (optional, format: YYYY-MM-DD)
- `doctorUserId`: Filter by specific doctor (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Appointment statistics retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": {
        "totalAppointments": 150,
        "statusDistribution": {
            "SCHEDULED": 45,
            "CONFIRMED": 35,
            "COMPLETED": 50,
            "CANCELLED": 15,
            "NO_SHOW": 5
        },
        "typeDistribution": {
            "CONSULTATION": 80,
            "FOLLOW_UP": 35,
            "CHECK_UP": 25,
            "EMERGENCY": 8,
            "PROCEDURE": 2
        },
        "monthlyTrends": [
            {
                "month": "2024-01",
                "totalAppointments": 45,
                "completedAppointments": 40,
                "cancelledAppointments": 5
            },
            {
                "month": "2024-02",
                "totalAppointments": 55,
                "completedAppointments": 48,
                "cancelledAppointments": 7
            }
        ],
        "doctorPerformance": [
            {
                "doctorUserId": 456,
                "doctorName": "Dr. Jane Smith",
                "totalAppointments": 75,
                "completedAppointments": 68,
                "cancelledAppointments": 7,
                "noShowAppointments": 0,
                "averageRating": 4.8
            }
        ],
        "busyTimeSlots": [
            {
                "timeSlot": "14:00:00-15:00:00",
                "appointmentCount": 25
            },
            {
                "timeSlot": "10:00:00-11:00:00",
                "appointmentCount": 22
            }
        ]
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/appointments/statistics?fromDate=2024-01-01&toDate=2024-02-29" \
-H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

### 13. Get Today's Appointments
**Endpoint:** `GET /api/appointments/today`

**Description:** Get all appointments scheduled for today

**Required Roles:** ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE

**Query Parameters:**
- `doctorUserId`: Filter by specific doctor (optional)
- `status`: Filter by appointment status (optional)

**Response (Success - 200):**
```json
{
    "success": true,
    "message": "Today's appointments retrieved successfully",
    "timestamp": "2024-01-15T10:30:00",
    "data": [
        {
            "id": 1,
            "patientId": 10,
            "doctorId": 5,
            "patientUserId": 123,
            "doctorUserId": 456,
            "appointmentDate": "2024-01-15",
            "appointmentTime": "14:30:00",
            "durationMinutes": 30,
            "status": "CONFIRMED",
            "appointmentType": "CONSULTATION",
            "reasonForVisit": "Regular checkup",
            "appointmentDateTime": "2024-01-15T14:30:00",
            "appointmentEndTime": "2024-01-15T15:00:00",
            "statusDisplay": "Confirmed",
            "typeDisplay": "Consultation",
            "patientName": "John Doe",
            "patientEmail": "john.doe@example.com",
            "doctorName": "Dr. Jane Smith",
            "doctorEmail": "dr.smith@hospital.com"
        }
    ],
    "summary": {
        "totalAppointments": 1,
        "confirmedAppointments": 1,
        "pendingAppointments": 0,
        "completedAppointments": 0
    }
}
```

**cURL Command:**
```bash
curl -X GET "http://localhost:9000/api/appointments/today?doctorUserId=456" \
-H "Authorization: Bearer DOCTOR_JWT_TOKEN"
```

---

## Kafka Integration

### Published Events

**Appointment Booked Event:**
- **Topic:** `appointment.booked`
- **Trigger:** When a new appointment is created
- **Message Format:**
```json
{
  "eventType": "APPOINTMENT_BOOKED",
  "appointmentId": 123,
  "patientUserId": 456,
  "doctorUserId": 789,
  "appointmentDate": "2024-02-15",
  "appointmentTime": "14:30:00",
  "type": "CONSULTATION",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Appointment Cancelled Event:**
- **Topic:** `appointment.cancelled`
- **Trigger:** When an appointment is cancelled
- **Message Format:**
```json
{
  "eventType": "APPOINTMENT_CANCELLED",
  "appointmentId": 123,
  "patientUserId": 456,
  "doctorUserId": 789,
  "cancellationReason": "Patient emergency",
  "cancelledBy": 456,
  "cancelledAt": "2024-01-15T10:35:00",
  "timestamp": "2024-01-15T10:35:00"
}
```

**Appointment Reminder Event:**
- **Topic:** `appointment.reminder`
- **Trigger:** Scheduled job 24 hours before appointment
- **Message Format:**
```json
{
  "eventType": "APPOINTMENT_REMINDER",
  "appointmentId": 123,
  "patientUserId": 456,
  "doctorUserId": 789,
  "appointmentDate": "2024-02-15",
  "appointmentTime": "14:30:00",
  "reminderType": "24_HOUR",
  "timestamp": "2024-01-15T10:30:00"
}
```

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
        "doctorUserId": "Doctor User ID is required",
        "appointmentDate": "Appointment date must be in the future",
        "appointmentTime": "Appointment time is required"
    },
    "path": "/api/appointments",
    "correlationId": "abc123-def456-ghi789"
}
```

**Business Logic Error (400):**
```json
{
    "success": false,
    "message": "Doctor is not available at the requested time",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/appointments",
    "correlationId": "abc123-def456-ghi789"
}
```

**Conflict Error (409):**
```json
{
    "success": false,
    "message": "Time slot conflicts with existing appointment",
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/appointments",
    "correlationId": "abc123-def456-ghi789"
}
```

**Authorization Error (403):**
```json
{
    "error": "Access Denied: Insufficient permissions to access this appointment",
    "status": 403,
    "timestamp": "2024-01-15T10:30:00",
    "path": "/api/appointments/123",
    "correlationId": "abc123-def456-ghi789"
}
```

---

## Security Considerations

### Access Control Matrix

| Operation | Admin | Doctor | Nurse | Patient | Notes |
|-----------|-------|--------|-------|---------|-------|
| Book Appointment | ✅ | ✅ | ❌ | ✅ | Patients can only book for themselves |
| View All Appointments | ✅ | ✅ | ✅ | ❌ | Staff can view all |
| View Own Appointments | ✅ | ✅ | ✅ | ✅ | All roles can view their own |
| Cancel Appointment | ✅ | ✅ | Limited | ✅ | Patients can only cancel their own |
| Reschedule Appointment | ✅ | ✅ | ❌ | ✅ | Patients can only reschedule their own |
| Update Status | ✅ | ✅ | ❌ | ❌ | Only medical professionals |
| Manage Availability | ✅ | ✅ | ❌ | ❌ | Doctors can manage own schedule |
| View Statistics | ✅ | ✅ | ❌ | ❌ | Analytics for medical professionals |

### Business Rules
1. **Advance Booking**: Appointments must be booked at least 2 hours in advance
2. **Cancellation Policy**: Appointments can be cancelled up to 2 hours before scheduled time
3. **Working Hours**: Appointments can only be scheduled during doctor's working hours
4. **Conflict Prevention**: System prevents double-booking of time slots
5. **Patient Limits**: Patients can have maximum 3 active appointments at any time
6. **Doctor Availability**: Doctors must set their availability before patients can book

---

## Integration Testing

### Complete Appointment Flow Test
```bash
#!/bin/bash

# 1. Login as patient
PATIENT_TOKEN=$(curl -s -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "patient_john", "password": "Patient123"}' | jq -r '.data.token')

# 2. Check doctor availability
curl -X GET "http://localhost:9000/api/appointments/availability/doctor/456?date=2024-02-15" \
-H "Authorization: Bearer $PATIENT_TOKEN"

# 3. Book appointment
APPOINTMENT_RESPONSE=$(curl -s -X POST http://localhost:9000/api/appointments \
-H "Authorization: Bearer $PATIENT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "doctorUserId": 456,
    "appointmentDate": "2024-02-15",
    "appointmentTime": "14:30:00",
    "appointmentType": "CONSULTATION",
    "reasonForVisit": "Regular checkup"
}')

APPOINTMENT_ID=$(echo $APPOINTMENT_RESPONSE | jq -r '.data.id')

# 4. View patient appointments
curl -X GET http://localhost:9000/api/appointments/patient \
-H "Authorization: Bearer $PATIENT_TOKEN"

# 5. Login as doctor
DOCTOR_TOKEN=$(curl -s -X POST http://localhost:9000/api/auth/signin \
-H "Content-Type: application/json" \
-d '{"username": "dr_smith", "password": "Doctor123"}' | jq -r '.data.token')

# 6. View doctor appointments
curl -X GET http://localhost:9000/api/appointments/doctor \
-H "Authorization: Bearer $DOCTOR_TOKEN"

# 7. Confirm appointment
curl -X PUT http://localhost:9000/api/appointments/$APPOINTMENT_ID/status \
-H "Authorization: Bearer $DOCTOR_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "status": "CONFIRMED",
    "notes": "Appointment confirmed"
}'

# 8. Cancel appointment (as patient)
curl -X DELETE http://localhost:9000/api/appointments/$APPOINTMENT_ID \
-H "Authorization: Bearer $PATIENT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "cancellationReason": "Unable to attend due to work emergency"
}'
```

---

## Performance Considerations

### Database Optimization
- Indexed fields: `doctor_user_id`, `patient_user_id`, `appointment_date`, `status`
- Compound indexes for common query patterns
- Partitioning by date for large datasets

### Caching Strategy
- Doctor availability cached for 1 hour
- Available time slots cached for 30 minutes
- Appointment statistics cached for 1 day

### Rate Limiting
- Appointment booking: 5 requests per minute per patient
- Availability checking: 20 requests per minute per user
- Statistics: 10 requests per minute per user

---

## Status Codes Summary

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request (Validation/Business Logic Error) |
| 401 | Unauthorized (Missing/Invalid JWT) |
| 403 | Forbidden (Insufficient Permissions) |
| 404 | Not Found (Appointment Not Found) |
| 409 | Conflict (Time Slot Conflict) |
| 500 | Internal Server Error |

---

## Environment Configuration

### Development
```properties
appointment.advance-booking-hours=2
appointment.max-appointments-per-patient=3
appointment.default-duration-minutes=30
appointment.reminder-hours=24
```

### Production
```properties
appointment.advance-booking-hours=4
appointment.max-appointments-per-patient=5
appointment.default-duration-minutes=30
appointment.reminder-hours=24
```
