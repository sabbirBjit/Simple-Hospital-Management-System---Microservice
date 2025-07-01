package com.hms.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityResponse {
    
    private Long id;
    private Long doctorUserId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private String dayDisplay;
    private String timeSlotDisplay;
    
    public DoctorAvailabilityResponse(Long id, Long doctorUserId, DayOfWeek dayOfWeek, 
                                    LocalTime startTime, LocalTime endTime, Boolean isAvailable,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctorUserId = doctorUserId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Set computed fields
        this.dayDisplay = dayOfWeek.toString();
        this.timeSlotDisplay = startTime.toString() + " - " + endTime.toString();
    }
}
