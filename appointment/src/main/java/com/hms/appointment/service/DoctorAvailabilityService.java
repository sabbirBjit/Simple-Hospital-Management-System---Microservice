package com.hms.appointment.service;

import com.hms.appointment.dto.DoctorAvailabilityRequest;
import com.hms.appointment.dto.DoctorAvailabilityResponse;
import com.hms.appointment.model.DoctorAvailability;
import com.hms.appointment.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorAvailabilityService {
    
    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public DoctorAvailabilityResponse updateAvailability(Long doctorUserId, DoctorAvailabilityRequest request) {
        // Check for overlapping availability
        List<DoctorAvailability> overlapping = availabilityRepository
            .findOverlappingAvailability(doctorUserId, request.getDayOfWeek(), 
                request.getStartTime(), request.getEndTime(), 0L);
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Overlapping availability exists for this time slot");
        }
        
        // Create or update availability
        Optional<DoctorAvailability> existing = availabilityRepository
            .findByDoctorUserIdAndDayOfWeek(doctorUserId, request.getDayOfWeek());
        
        DoctorAvailability availability;
        if (existing.isPresent()) {
            availability = existing.get();
            availability.setStartTime(request.getStartTime());
            availability.setEndTime(request.getEndTime());
            availability.setIsAvailable(request.getIsAvailable());
        } else {
            availability = new DoctorAvailability();
            availability.setDoctorUserId(doctorUserId);
            availability.setDayOfWeek(request.getDayOfWeek());
            availability.setStartTime(request.getStartTime());
            availability.setEndTime(request.getEndTime());
            availability.setIsAvailable(request.getIsAvailable());
        }
        
        DoctorAvailability savedAvailability = availabilityRepository.save(availability);
        
        // Trigger Kafka event for availability change
        String availabilityEventData = String.format(
            "{\"doctorUserId\":%d,\"dayOfWeek\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"isAvailable\":%b}", 
            doctorUserId,
            savedAvailability.getDayOfWeek().toString(),
            savedAvailability.getStartTime().toString(),
            savedAvailability.getEndTime().toString(),
            savedAvailability.getIsAvailable()
        );
        
        kafkaTemplate.send("doctor.availability.updated", availabilityEventData);
        
        return mapToResponse(savedAvailability);
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
            .findAvailabilityForTimeSlot(doctorUserId, dayOfWeek, time, endTime);
        
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
