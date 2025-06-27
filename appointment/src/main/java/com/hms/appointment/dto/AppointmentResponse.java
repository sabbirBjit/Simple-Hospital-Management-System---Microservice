package com.hms.appointment.dto;

import com.hms.appointment.model.AppointmentStatus;
import com.hms.appointment.model.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long patientUserId;
    private Long doctorUserId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Integer durationMinutes;
    private AppointmentStatus status;
    private AppointmentType appointmentType;
    private String reasonForVisit;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private LocalDateTime cancelledAt;
    private Long cancelledBy;
    private String cancellationReason;
    
    // Computed fields
    private LocalDateTime appointmentDateTime;
    private LocalDateTime appointmentEndTime;
    private String statusDisplay;
    private String typeDisplay;
    
    // Patient and Doctor information (populated from external services)
    private String patientName;
    private String patientEmail;
    private String doctorName;
    private String doctorEmail;
    
    public AppointmentResponse(Long id, Long patientId, Long doctorId, Long patientUserId, 
                              Long doctorUserId, LocalDate appointmentDate, LocalTime appointmentTime,
                              Integer durationMinutes, AppointmentStatus status, AppointmentType appointmentType,
                              String reasonForVisit, String notes, LocalDateTime createdAt, 
                              LocalDateTime updatedAt, Long createdBy) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientUserId = patientUserId;
        this.doctorUserId = doctorUserId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.appointmentType = appointmentType;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        
        // Set computed fields
        this.appointmentDateTime = appointmentDate.atTime(appointmentTime);
        this.appointmentEndTime = this.appointmentDateTime.plusMinutes(durationMinutes);
        this.statusDisplay = status.getDisplayName();
        this.typeDisplay = appointmentType.getDisplayName();
    }
}
