package com.hms.appointment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.appointment.dto.DoctorAvailabilityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private DoctorAvailabilityService doctorAvailabilityService;

    @KafkaListener(topics = "user.created", groupId = "appointment-service-group")
    public void handleUserCreated(String message) {
        try {
            logger.info("Received user.created event: {}", message);
            JsonNode userEvent = objectMapper.readTree(message);
            
            Long userId = userEvent.get("userId").asLong();
            String email = userEvent.get("email").asText();
            String firstName = userEvent.get("firstName").asText();
            String lastName = userEvent.get("lastName").asText();
            
            // Check if user has ROLE_DOCTOR for doctor availability setup
            if (userEvent.has("roles")) {
                JsonNode rolesNode = userEvent.get("roles");
                if (rolesNode.isArray()) {
                    for (JsonNode roleNode : rolesNode) {
                        if ("ROLE_DOCTOR".equals(roleNode.asText())) {
                            logger.info("New doctor registered - userId: {}, name: {} {}", userId, firstName, lastName);
                            // Auto-create default availability schedule
                            createDefaultDoctorSchedule(userId);
                            break;
                        }
                    }
                }
            }
            
            logger.info("Processed user creation event for userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error processing user.created event: {}", e.getMessage(), e);
        }
    }
    
    private void createDefaultDoctorSchedule(Long doctorUserId) {
        try {
            List<DoctorAvailabilityRequest> defaultSchedule = new ArrayList<>();
            
            // Create default Monday-Friday 9-5 schedule
            for (DayOfWeek day : DayOfWeek.values()) {
                DoctorAvailabilityRequest request = new DoctorAvailabilityRequest();
                request.setDayOfWeek(day);
                
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    request.setIsAvailable(false);
                } else {
                    request.setStartTime(LocalTime.of(9, 0));
                    request.setEndTime(LocalTime.of(17, 0));
                    request.setIsAvailable(true);
                }
                
                defaultSchedule.add(request);
            }
            
            doctorAvailabilityService.setWeeklySchedule(doctorUserId, defaultSchedule);
            logger.info("Created default availability schedule for doctor userId: {}", doctorUserId);
            
        } catch (Exception e) {
            logger.error("Error creating default schedule for doctor userId {}: {}", doctorUserId, e.getMessage());
        }
    }

    @KafkaListener(topics = "patient.registered", groupId = "appointment-service-group")
    public void handlePatientRegistered(String message) {
        try {
            logger.info("Received patient.registered event: {}", message);
            JsonNode patientEvent = objectMapper.readTree(message);
            
            Long patientId = patientEvent.get("patientId").asLong();
            Long userId = patientEvent.get("userId").asLong();
            String email = patientEvent.get("email").asText();
            String firstName = patientEvent.get("firstName").asText();
            String lastName = patientEvent.get("lastName").asText();
            
            // Cache patient information for quick lookup during appointment booking
            logger.info("Patient registered - patientId: {}, userId: {}, name: {} {}", 
                       patientId, userId, firstName, lastName);
            
            // Future: Could update local patient cache or send welcome appointment booking instructions
            
        } catch (Exception e) {
            logger.error("Error processing patient.registered event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user.updated", groupId = "appointment-service-group")
    public void handleUserUpdated(String message) {
        try {
            logger.info("Received user.updated event: {}", message);
            JsonNode userEvent = objectMapper.readTree(message);
            
            Long userId = userEvent.get("userId").asLong();
            String email = userEvent.get("email").asText();
            String firstName = userEvent.get("firstName").asText();
            String lastName = userEvent.get("lastName").asText();
            
            // Update cached user information if maintained locally
            logger.info("Processing user update for userId: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error processing user.updated event: {}", e.getMessage(), e);
        }
    }
}
