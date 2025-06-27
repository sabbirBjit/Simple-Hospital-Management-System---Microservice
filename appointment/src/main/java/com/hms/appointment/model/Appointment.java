package com.hms.appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_id", nullable = false)
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @Column(name = "doctor_id", nullable = false)
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @Column(name = "patient_user_id", nullable = false)
    @NotNull(message = "Patient User ID is required")
    private Long patientUserId;
    
    @Column(name = "doctor_user_id", nullable = false)
    @NotNull(message = "Doctor User ID is required")
    private Long doctorUserId;
    
    @Column(name = "appointment_date", nullable = false)
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;
    
    @Column(name = "appointment_time", nullable = false)
    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;
    
    @Column(name = "duration_minutes")
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration cannot exceed 480 minutes")
    private Integer durationMinutes = 30;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type")
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Column(name = "reason_for_visit", columnDefinition = "TEXT")
    @Size(max = 1000, message = "Reason for visit must not exceed 1000 characters")
    private String reasonForVisit;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_by")
    private Long cancelledBy;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String cancellationReason;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDate.atTime(appointmentTime);
    }
    
    public LocalDateTime getAppointmentEndTime() {
        return getAppointmentDateTime().plusMinutes(durationMinutes);
    }
    
    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED;
    }
    
    public boolean canBeCancelled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }
    
    public boolean canBeRescheduled() {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }
}
