package com.hms.appointment.service;

import com.hms.appointment.dto.DoctorAvailabilityRequest;
import com.hms.appointment.dto.DoctorAvailabilityResponse;
import com.hms.appointment.model.Appointment;
import com.hms.appointment.model.AppointmentStatus;
import com.hms.appointment.model.DoctorAvailability;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.repository.DoctorAvailabilityRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorAvailabilityService {
    
    private static final Logger logger = LoggerFactory.getLogger(DoctorAvailabilityService.class);

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public DoctorAvailabilityResponse updateAvailability(Long doctorUserId, DoctorAvailabilityRequest request) {


        // Validate required fields
        if (request.getDayOfWeek() == null) {
            throw new IllegalArgumentException("Day of week is required");
        }
        
        if (Boolean.TRUE.equals(request.getIsAvailable()) && 
            (request.getStartTime() == null || request.getEndTime() == null)) {
            throw new IllegalArgumentException("Start time and end time are required when available is true");
        }
        
        // Check for overlapping availability only if available
        if (Boolean.TRUE.equals(request.getIsAvailable())) {
            List<DoctorAvailability> overlapping = availabilityRepository
                .findOverlappingAvailability(doctorUserId, request.getDayOfWeek(), 
                    request.getStartTime(), request.getEndTime(), 0L, LocalTime.of(0, 0));
            
            if (!overlapping.isEmpty()) {
                throw new RuntimeException("Overlapping availability exists for this time slot");
            }
        }
        
        // Create or update availability
        Optional<DoctorAvailability> existing = availabilityRepository
            .findByDoctorUserIdAndDayOfWeek(doctorUserId, request.getDayOfWeek());
        
        DoctorAvailability availability;
        if (existing.isPresent()) {
            availability = existing.get();
            if (Boolean.TRUE.equals(request.getIsAvailable())) {
                availability.setStartTime(request.getStartTime());
                availability.setEndTime(request.getEndTime());
            } else {
                // For unavailable days, set default times (00:00:00 to 00:01:00)
                availability.setStartTime(LocalTime.of(0, 0));
                availability.setEndTime(LocalTime.of(0, 1));
            }
            availability.setIsAvailable(request.getIsAvailable());
        } else {
            availability = new DoctorAvailability();
            availability.setDoctorUserId(doctorUserId);
            availability.setDayOfWeek(request.getDayOfWeek());
            if (Boolean.TRUE.equals(request.getIsAvailable())) {
                availability.setStartTime(request.getStartTime());
                availability.setEndTime(request.getEndTime());
            } else {
                // For unavailable days, set default times (00:00:00 to 00:01:00)
                availability.setStartTime(LocalTime.of(0, 0));
                availability.setEndTime(LocalTime.of(0, 1));
            }
            availability.setIsAvailable(request.getIsAvailable());
        }
        
        DoctorAvailability savedAvailability = availabilityRepository.save(availability);
        
        // Trigger Kafka event for availability change
        String availabilityEventData = String.format(
            "{\"doctorUserId\":%d,\"dayOfWeek\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"isAvailable\":%b}", 
            doctorUserId,
            savedAvailability.getDayOfWeek().toString(),
            savedAvailability.getStartTime() != null ? savedAvailability.getStartTime().toString() : "null",
            savedAvailability.getEndTime() != null ? savedAvailability.getEndTime().toString() : "null",
            savedAvailability.getIsAvailable()
        );
        
        kafkaTemplate.send("doctor.availability.updated", availabilityEventData);
        
        return mapToResponse(savedAvailability);
    }
    
    public DoctorAvailabilityResponse setAvailability(Long doctorUserId, DoctorAvailabilityRequest request) {
        return updateAvailability(doctorUserId, request);
    }
    
    public List<DoctorAvailabilityResponse> setWeeklySchedule(Long doctorUserId, List<DoctorAvailabilityRequest> weeklySchedule) {
        List<DoctorAvailabilityResponse> responses = new ArrayList<>();
        
        for (DoctorAvailabilityRequest request : weeklySchedule) {
            try {
                // Validate each request
                if (request.getDayOfWeek() == null) {
                    logger.error("Day of week is null for weekly schedule item");
                    continue;
                }
                
                DoctorAvailabilityResponse response = setAvailability(doctorUserId, request);
                responses.add(response);
            } catch (Exception e) {
                // Log error but continue with other days
                logger.error("Error setting availability for {}: {}", 
                    request.getDayOfWeek(), e.getMessage());
            }
        }
        
        return responses;
    }
    
    public DoctorAvailabilityResponse getAvailabilityForDate(Long doctorUserId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorAvailability> availability = availabilityRepository
            .findByDoctorUserIdAndDayOfWeekAndIsAvailableTrue(doctorUserId, dayOfWeek, LocalTime.of(0, 0));
        
        if (availability.isEmpty()) {
            throw new RuntimeException("Doctor is not available on " + dayOfWeek.toString());
        }
        
        return mapToResponse(availability.get());
    }
    
    public List<LocalTime> getAvailableTimeSlots(Long doctorUserId, LocalDate date, int durationMinutes) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorAvailability> availability = availabilityRepository
            .findByDoctorUserIdAndDayOfWeekAndIsAvailableTrue(doctorUserId, dayOfWeek, LocalTime.of(0, 0));
        
        if (availability.isEmpty()) {
            return new ArrayList<>();
        }
        
        DoctorAvailability doctorAvailability = availability.get();
        
        // Check if this is an unavailable day (00:00:00 to 00:01:00)
        if (doctorAvailability.getStartTime().equals(LocalTime.of(0, 0)) && 
            doctorAvailability.getEndTime().equals(LocalTime.of(0, 1))) {
            return new ArrayList<>();
        }
        
        List<LocalTime> availableSlots = new ArrayList<>();
        
        // Get existing appointments for the date
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorUserIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorUserId, date);
        
        // Generate time slots
        LocalTime currentSlot = doctorAvailability.getStartTime();
        LocalTime endTime = doctorAvailability.getEndTime();
        
        while (currentSlot.plusMinutes(durationMinutes).compareTo(endTime) <= 0) {
            LocalTime slotStartTime = currentSlot; // Create a final copy for lambda
            LocalTime slotEndTime = currentSlot.plusMinutes(durationMinutes);
            
            // Check if slot conflicts with existing appointments
            boolean isSlotAvailable = existingAppointments.stream().noneMatch(appointment -> {
                if (appointment.getStatus() == AppointmentStatus.CANCELLED) return false;
                
                LocalTime appointmentStart = appointment.getAppointmentTime();
                LocalTime appointmentEnd = appointmentStart.plusMinutes(appointment.getDurationMinutes());
                
                return slotStartTime.isBefore(appointmentEnd) && slotEndTime.isAfter(appointmentStart);
            });
            
            if (isSlotAvailable) {
                availableSlots.add(slotStartTime);
            }
            
            currentSlot = currentSlot.plusMinutes(30); // 30-minute slots
        }
        
        return availableSlots;
    }
    
    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorUserId) {
        List<DoctorAvailability> availabilities = availabilityRepository
            .findByDoctorUserIdOrderByDayOfWeekAscStartTimeAsc(doctorUserId);
        
        return availabilities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public boolean isDoctorAvailable(Long doctorUserId, DayOfWeek dayOfWeek, LocalTime time, int durationMinutes) {
        LocalTime endTime = time.plusMinutes(durationMinutes);
        Optional<DoctorAvailability> availability = availabilityRepository
            .findAvailabilityForTimeSlot(doctorUserId, dayOfWeek, time, endTime, LocalTime.of(0, 0));
        
        return availability.isPresent();
    }
    
    private DoctorAvailabilityResponse mapToResponse(DoctorAvailability availability) {
        return new DoctorAvailabilityResponse(
            availability.getId(),
            availability.getDoctorUserId(),
            availability.getDayOfWeek(),
            availability.getStartTime(),
            availability.getEndTime(),
            availability.getIsAvailable(),
            availability.getCreatedAt(),
            availability.getUpdatedAt()
        );
    }
}
