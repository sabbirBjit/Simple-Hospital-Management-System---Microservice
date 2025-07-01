package com.hms.appointment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_availability", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_user_id", "day_of_week"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "doctor_user_id", nullable = false)
    @NotNull(message = "Doctor User ID is required")
    private Long doctorUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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
    public boolean isTimeSlotAvailable(LocalTime time, int durationMinutes) {
        if (!isAvailable || startTime == null || endTime == null) {
            return false;
        }
        
        // Check if this is an unavailable day (00:00:00 to 00:01:00)
        if (startTime.equals(LocalTime.of(0, 0)) && endTime.equals(LocalTime.of(0, 1))) {
            return false;
        }
        
        LocalTime endTimeForSlot = time.plusMinutes(durationMinutes);
        return !time.isBefore(startTime) && !endTimeForSlot.isAfter(endTime);
    }
    
    public String getDisplayText() {
        if (!isAvailable || startTime == null || endTime == null) {
            return String.format("%s: Not Available", dayOfWeek.toString());
        }
        
        // Check if this is an unavailable day (00:00:00 to 00:01:00)
        if (startTime.equals(LocalTime.of(0, 0)) && endTime.equals(LocalTime.of(0, 1))) {
            return String.format("%s: Not Available", dayOfWeek.toString());
        }
        
        return String.format("%s: %s - %s", 
            dayOfWeek.toString(), 
            startTime.toString(), 
            endTime.toString());
    }
}
