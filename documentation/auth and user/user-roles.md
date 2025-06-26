# User Roles and Permissions Documentation

## Overview
The Hospital Management System implements a role-based access control (RBAC) system with four distinct user roles. Each role has specific permissions and access levels throughout the system, with special emphasis on patient data protection and medical information access control.

## Role Hierarchy

```
ROLE_ADMIN (Highest Authority)
    ├── Full system access
    ├── User management
    ├── System configuration
    ├── Patient data management
    └── All lower role permissions

ROLE_DOCTOR
    ├── Patient medical records (full access)
    ├── Appointment management
    ├── Prescription management
    ├── Medical history access
    ├── Patient statistics and analytics
    └── Medical professional privileges

ROLE_NURSE
    ├── Patient basic information
    ├── Appointment viewing/basic management
    ├── Medical record viewing (read-only)
    ├── Patient care coordination
    └── Contact information updates

ROLE_PATIENT (Base Level)
    ├── Own profile management
    ├── Own medical records (read-only)
    ├── Appointment booking
    ├── Profile completeness tracking
    └── Basic system interaction
```

---

## Enhanced Role Definitions

### 1. ROLE_ADMIN
**Description:** System administrators with full access to all features and data.

**Core Responsibilities:**
- System configuration and maintenance
- User account management
- Security oversight
- System monitoring and reporting
- Data backup and recovery oversight

**Permissions:**
- ✅ Create, read, update, delete all users
- ✅ Assign and modify user roles
- ✅ Access all patient records
- ✅ View all appointments across the system
- ✅ Access system logs and analytics
- ✅ Configure system settings
- ✅ Manage doctor availability
- ✅ Override appointment restrictions
- ✅ Access all notification settings
- ✅ System backup and restore operations

**API Access:**
- All authentication endpoints
- All user management endpoints
- All patient endpoints
- All appointment endpoints
- All notification endpoints
- System management endpoints

---

### 2. ROLE_DOCTOR
**Description:** Medical professionals who provide patient care and manage medical records.

**Core Responsibilities:**
- Patient diagnosis and treatment
- Medical record management
- Appointment scheduling and management
- Prescription management
- Medical consultations

**Permissions:**
- ✅ View and update patient medical records
- ✅ Create and manage appointments
- ✅ Access patient history and charts
- ✅ Manage own availability schedule
- ✅ Create and update prescriptions
- ✅ View other users (patients, nurses) - limited info
- ✅ Update own profile
- ❌ Create or delete user accounts
- ❌ Modify other users' roles
- ❌ Access system configuration

**API Access:**
- Authentication endpoints
- User viewing endpoints (limited)
- All patient endpoints
- All appointment endpoints (full access)
- Own profile management
- Medical record endpoints

---

### 3. ROLE_NURSE
**Description:** Healthcare support professionals who assist in patient care coordination.

**Core Responsibilities:**
- Patient care coordination
- Basic appointment management
- Patient information updates
- Medical record viewing
- Communication facilitation

**Permissions:**
- ✅ View patient basic information
- ✅ Update patient contact information
- ✅ View appointments and basic scheduling
- ✅ View medical records (read-only)
- ✅ Manage patient check-ins
- ✅ View other users (basic info)
- ✅ Update own profile
- ❌ Create or delete patient records
- ❌ Modify medical diagnoses
- ❌ Create or delete user accounts
- ❌ Access system configuration

**API Access:**
- Authentication endpoints
- User viewing endpoints (limited)
- Patient endpoints (read and basic updates)
- Appointment viewing endpoints
- Own profile management

---

### 4. ROLE_PATIENT (Enhanced)
**Description:** Patients who access their own medical information and manage appointments with comprehensive profile management capabilities.

**Core Responsibilities:**
- Personal health information management
- Appointment booking and management
- Profile updates and completeness tracking
- Medical record viewing (own records only)
- Emergency contact management
- Insurance information management

**Enhanced Permissions:**
- ✅ View and update own comprehensive profile
- ✅ View own medical records and history
- ✅ Book and manage own appointments
- ✅ View own appointment history
- ✅ Update own contact and address information
- ✅ Manage own emergency contacts
- ✅ Update insurance information
- ✅ View profile completeness status
- ✅ Track own medical conditions and allergies
- ✅ Manage medication list
- ✅ Change own password
- ✅ Access own patient statistics
- ❌ Access other patients' information
- ❌ View system users
- ❌ Access administrative functions
- ❌ Modify medical diagnoses or prescriptions
- ❌ Access patient analytics or system-wide data

**API Access (Enhanced):**
- Authentication endpoints
- Own profile management (`/api/patients/my-profile`)
- Own medical records (read-only)
- Own appointment management
- Limited patient endpoints (own data only)
- Profile update endpoints (`/api/patients/profile/*`)

**Patient-Specific Features:**
- **Profile Completeness Tracking**: System tracks completion of required profile fields
- **Medical Information Management**: Comprehensive management of allergies, medications, and conditions
- **Insurance Integration**: Full insurance provider and policy information management
- **Age Calculation**: Automatic age calculation and updates
- **Emergency Contacts**: Management of multiple emergency contacts
- **Data Export**: Ability to export own medical data (GDPR compliance)

---

## Permission Matrix

| Feature | Admin | Doctor | Nurse | Patient |
|---------|-------|--------|-------|---------|
| **User Management** |
| Create users | ✅ | ❌ | ❌ | ❌ |
| View all users | ✅ | Limited | Limited | ❌ |
| Update any user | ✅ | ❌ | ❌ | ❌ |
| Delete users | ✅ | ❌ | ❌ | ❌ |
| Assign roles | ✅ | ❌ | ❌ | ❌ |
| **Patient Records** |
| View all patients | ✅ | ✅ | ✅ | ❌ |
| Create patient records | ✅ | ✅ | ❌ | ❌ |
| Update medical records | ✅ | ✅ | ❌ | ❌ |
| Delete patient records | ✅ | ❌ | ❌ | ❌ |
| View own records | ✅ | ✅ | ✅ | ✅ |
| **Appointments** |
| View all appointments | ✅ | ✅ | ✅ | ❌ |
| Create appointments | ✅ | ✅ | Limited | ✅* |
| Update appointments | ✅ | ✅ | Limited | ✅* |
| Delete appointments | ✅ | ✅ | ❌ | ✅* |
| Manage doctor availability | ✅ | ✅** | ❌ | ❌ |
| **System Features** |
| Access system logs | ✅ | ❌ | ❌ | ❌ |
| System configuration | ✅ | ❌ | ❌ | ❌ |
| Notification management | ✅ | Limited | Limited | Limited |

*Own appointments only
**Own availability only

---

## Role Assignment Guidelines

### Default Role Assignment
- New user registrations default to `ROLE_PATIENT`
- Role upgrades require admin approval
- Multiple roles can be assigned to a single user if needed

### Role Change Process
1. User requests role change through admin
2. Admin reviews credentials and requirements
3. Admin updates user role in system
4. User receives notification of role change
5. New permissions take effect immediately

### Security Considerations
- Roles are immutable once assigned (admin-only changes)
- Role escalation requires proper authentication
- All role changes are logged for audit purposes
- Minimum principle of least privilege applied

---

## Implementation Details

### Spring Security Annotations
```java
// Admin only
@PreAuthorize("hasRole('ADMIN')")

// Doctor and above
@PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")

// All medical staff
@PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")

// Own resource access
@PreAuthorize("hasRole('ADMIN') or (hasRole('PATIENT') and #id == authentication.principal.id)")
```

### JWT Token Claims
```json
{
  "sub": "username",
  "iat": 1640995200,
  "exp": 1641081600,
  "roles": ["ROLE_PATIENT"],
  "userId": 123,
  "firstName": "John",
  "lastName": "Doe"
}
```

### Database Role Storage
```sql
-- Roles table
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name ENUM('ROLE_ADMIN', 'ROLE_DOCTOR', 'ROLE_NURSE', 'ROLE_PATIENT'),
    description VARCHAR(255)
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

---

## Role-based API Examples

### Admin Creating a Doctor
```bash
curl -X POST http://localhost:9000/api/auth/signup \
-H "Authorization: Bearer ADMIN_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "username": "dr_wilson",
    "email": "dr.wilson@hospital.com",
    "password": "Doctor123",
    "firstName": "Dr. Sarah",
    "lastName": "Wilson",
    "phoneNumber": "+1234567894",
    "roles": ["doctor"]
}'
```

### Doctor Viewing Patients
```bash
curl -X GET http://localhost:9000/api/patients \
-H "Authorization: Bearer DOCTOR_JWT_TOKEN"
```

### Patient Viewing Own Records
```bash
curl -X GET http://localhost:9000/api/patients/own \
-H "Authorization: Bearer PATIENT_JWT_TOKEN"
```

### Nurse Updating Patient Contact Info
```bash
curl -X PUT http://localhost:9000/api/patients/123/contact \
-H "Authorization: Bearer NURSE_JWT_TOKEN" \
-H "Content-Type: application/json" \
-d '{
    "phoneNumber": "+1234567899",
    "emergencyContact": "Jane Doe - +1234567800"
}'
```

---

## Best Practices

### For Administrators
- Regularly review user roles and permissions
- Monitor role assignment patterns
- Implement regular access audits
- Maintain principle of least privilege

### For Developers
- Always use role-based annotations
- Validate permissions at multiple layers
- Log all permission-sensitive operations
- Test with different role scenarios

### For Users
- Request appropriate role levels for job functions
- Report any unauthorized access immediately
- Keep credentials secure
- Understand role limitations and capabilities

---

## Troubleshooting

### Common Permission Issues
1. **403 Forbidden Error**
   - Check user role assignments
   - Verify JWT token contains correct roles
   - Ensure endpoint requires appropriate role

2. **Role Not Updating**
   - Clear JWT tokens and re-login
   - Verify role assignment in database
   - Check for caching issues

3. **Multiple Role Conflicts**
   - Review role hierarchy
   - Check for conflicting permissions
   - Verify Spring Security configuration

### Debug Commands
```bash
# Check user roles
curl -X GET http://localhost:9000/api/users/me \
-H "Authorization: Bearer JWT_TOKEN"

# Validate token and roles
curl -X POST http://localhost:9000/api/auth/validate \
-H "Content-Type: application/json" \
-d '{"token": "JWT_TOKEN_HERE"}'
```
