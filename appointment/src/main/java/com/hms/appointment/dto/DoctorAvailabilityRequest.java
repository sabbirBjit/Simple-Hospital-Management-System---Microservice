package com.hms.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityRequest {
    
    // Remove @NotNull here since it's optional for weekly schedule requests
    private DayOfWeek dayOfWeek;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private Boolean isAvailable = true;
    
    // For weekly schedule setup
    private Long doctorUserId;
    private List<DoctorAvailabilityRequest> weeklySchedule;
    
    // Add validation method
    public boolean isValidSingleDayRequest() {
        return dayOfWeek != null && 
               (Boolean.FALSE.equals(isAvailable) || 
                (startTime != null && endTime != null));
    }
    
    public boolean isValidWeeklyScheduleRequest() {
        return weeklySchedule != null && !weeklySchedule.isEmpty();
    }
}
