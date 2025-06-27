package com.hms.appointment.dto;

import com.hms.appointment.model.AppointmentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    
    @NotNull(message = "Doctor User ID is required")
    private Long doctorUserId;
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;
    
    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;
    
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 480, message = "Duration cannot exceed 480 minutes")
    private Integer durationMinutes = 30;
    
    private AppointmentType appointmentType = AppointmentType.CONSULTATION;
    
    @Size(max = 1000, message = "Reason for visit must not exceed 1000 characters")
    private String reasonForVisit;
    
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}
