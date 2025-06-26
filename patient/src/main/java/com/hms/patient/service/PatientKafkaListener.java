package com.hms.patient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.patient.dto.PatientRequest;
import com.hms.patient.model.Gender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PatientKafkaListener {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientKafkaListener.class);
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @KafkaListener(topics = "user.patient.created", groupId = "patient-service-group")
    public void handlePatientUserCreated(String message) {
        try {
            logger.info("Received patient user creation event: {}", message);
            
            // Parse the JSON message
            Map<String, Object> userData = objectMapper.readValue(message, Map.class);
            
            // Create basic patient profile
            PatientRequest patientRequest = new PatientRequest();
            patientRequest.setUserId(((Number) userData.get("userId")).longValue());
            patientRequest.setFirstName((String) userData.get("firstName"));
            patientRequest.setLastName((String) userData.get("lastName"));
            patientRequest.setEmail((String) userData.get("email"));
            patientRequest.setPhoneNumber((String) userData.get("phoneNumber"));
            
            // Set default values that need to be updated later
            patientRequest.setGender(Gender.OTHER); // Default, to be updated by patient
            patientRequest.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1)); // Default, to be updated
            
            // Check if patient already exists
            if (!patientService.existsByUserId(patientRequest.getUserId())) {
                patientService.createPatient(patientRequest);
                logger.info("Patient profile created successfully for user ID: {}", patientRequest.getUserId());
            } else {
                logger.warn("Patient profile already exists for user ID: {}", patientRequest.getUserId());
            }
            
        } catch (org.apache.kafka.common.errors.TimeoutException e) {
            logger.warn("Kafka timeout - service will continue without event processing: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing patient user creation event: {}", e.getMessage(), e);
        }
    }
}
