package com.hms.patient.service;

import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientProfileUpdateRequest;
import com.hms.patient.model.Patient;
import com.hms.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private AuthServiceClient authServiceClient;
    
    public PatientResponse createPatient(PatientRequest request) {
        // Check if patient already exists
        if (patientRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("Patient already exists for user ID: " + request.getUserId());
        }
        
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Patient with email already exists: " + request.getEmail());
        }
        
        Patient patient = mapToEntity(request);
        Patient savedPatient = patientRepository.save(patient);
        
        // Send Kafka event with error handling
        try {
            kafkaTemplate.send("patient.registered", "Patient registered: " + savedPatient.getId());
        } catch (Exception e) {
            logger.warn("Failed to send Kafka event for patient registration: {}", e.getMessage());
            // Continue execution - don't fail the operation due to messaging issues
        }
        
        logger.info("Patient created successfully with ID: {}", savedPatient.getId());
        return mapToResponse(savedPatient);
    }
    
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findByIsActive(true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<PatientResponse> getPatientById(Long id) {
        return patientRepository.findById(id)
                .filter(patient -> patient.getIsActive())
                .map(this::mapToResponse);
    }
    
    public Optional<PatientResponse> getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .filter(patient -> patient.getIsActive())
                .map(this::mapToResponse);
    }
    
    public List<PatientResponse> searchPatientsByName(String name) {
        return patientRepository.findByNameContaining(name).stream()
                .filter(patient -> patient.getIsActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PatientResponse> getPatientsByCity(String city) {
        return patientRepository.findByCity(city).stream()
                .filter(patient -> patient.getIsActive())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        
        // Check if email is being changed and if it's already taken
        if (!patient.getEmail().equals(request.getEmail()) && 
            patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use by another patient");
        }
        
        updatePatientFromRequest(patient, request);
        Patient updatedPatient = patientRepository.save(patient);
        
        logger.info("Patient updated successfully with ID: {}", updatedPatient.getId());
        return mapToResponse(updatedPatient);
    }
    
    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
        
        // Soft delete
        patient.setIsActive(false);
        patientRepository.save(patient);
        
        logger.info("Patient soft deleted successfully with ID: {}", id);
    }
    
    public boolean existsByUserId(Long userId) {
        return patientRepository.existsByUserId(userId);
    }
    
    public Optional<PatientResponse> getPatientByUsername(String username) {
        try {
            logger.info("Getting patient by username: {}", username);
            
            // Get user details from auth service
            Optional<AuthServiceClient.UserDto> userOpt = authServiceClient.getUserByUsername(username);
            if (userOpt.isEmpty()) {
                logger.warn("User not found with username: {}", username);
                return Optional.empty();
            }
            
            // Get patient by user ID
            return getPatientByUserId(userOpt.get().getId());
            
        } catch (Exception e) {
            logger.error("Error getting patient by username {}: {}", username, e.getMessage());
            return Optional.empty();
        }
    }
    
    public PatientResponse updatePatientProfile(String username, PatientRequest request) {
        try {
            logger.info("Updating patient profile by username: {}", username);
            
            // Get user details from auth service
            Optional<AuthServiceClient.UserDto> userOpt = authServiceClient.getUserByUsername(username);
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found with username: " + username);
            }
            
            // Update patient profile using user ID
            return updatePatientProfileByUserId(userOpt.get().getId(), request);
            
        } catch (Exception e) {
            logger.error("Error updating patient profile by username {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to update patient profile: " + e.getMessage());
        }
    }
    
    // Method for updating with PatientProfileUpdateRequest (partial update)
    public PatientResponse updatePatientProfileByUserId(Long userId, PatientProfileUpdateRequest updateRequest) {
        Optional<Patient> patientOpt = patientRepository.findByUserId(userId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient profile not found for user ID: " + userId);
        }
        
        Patient patient = patientOpt.get();
        
        // Use helper method to update fields conditionally
        updatePatientFieldsFromRequest(patient, updateRequest);
        
        Patient updatedPatient = patientRepository.save(patient);
        
        logger.info("Patient profile updated successfully for user ID: {}", userId);
        return mapToResponse(updatedPatient);
    }

    // Overloaded method for updating with PatientRequest (full update)
    public PatientResponse updatePatientProfileByUserId(Long userId, PatientRequest request) {
        Optional<Patient> patientOpt = patientRepository.findByUserId(userId);
        if (patientOpt.isEmpty()) {
            throw new RuntimeException("Patient profile not found for user ID: " + userId);
        }
        
        Patient patient = patientOpt.get();
        
        // Check if email is being changed and if it's already taken
        if (!patient.getEmail().equals(request.getEmail()) && 
            patientRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use by another patient");
        }
        
        updatePatientFromRequest(patient, request);
        Patient updatedPatient = patientRepository.save(patient);
        
        logger.info("Patient profile fully updated successfully for user ID: {}", userId);
        return mapToResponse(updatedPatient);
    }

    private Patient mapToEntity(PatientRequest request) {
        Patient patient = new Patient();
        patient.setUserId(request.getUserId());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setCountry(request.getCountry());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setMedications(request.getMedications());
        patient.setMedicalConditions(request.getMedicalConditions());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
        patient.setIsActive(true);
        return patient;
    }
    
    private void updatePatientFromRequest(Patient patient, PatientRequest request) {
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhoneNumber(request.getPhoneNumber());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setCountry(request.getCountry());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setMedications(request.getMedications());
        patient.setMedicalConditions(request.getMedicalConditions());
        patient.setInsuranceProvider(request.getInsuranceProvider());
        patient.setInsurancePolicyNumber(request.getInsurancePolicyNumber());
    }
    
    private PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getUserId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getDateOfBirth(),
                patient.getAge(),
                patient.getGender(),
                patient.getAddress(),
                patient.getCity(),
                patient.getState(),  // Fixed: changed from setState() to getState()
                patient.getZipCode(),
                patient.getCountry(),
                patient.getBloodType(),
                patient.getAllergies(),
                patient.getMedications(),
                patient.getMedicalConditions(),
                patient.getInsuranceProvider(),
                patient.getInsurancePolicyNumber(),
                patient.getIsActive(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }
    
    private void updatePatientFieldsFromRequest(Patient patient, PatientProfileUpdateRequest updateRequest) {
        if (updateRequest.getPhoneNumber() != null) {
            patient.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getDateOfBirth() != null) {
            patient.setDateOfBirth(updateRequest.getDateOfBirth());
        }
        if (updateRequest.getGender() != null) {
            patient.setGender(updateRequest.getGender());
        }
        if (updateRequest.getAddress() != null) {
            patient.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getCity() != null) {
            patient.setCity(updateRequest.getCity());
        }
        if (updateRequest.getState() != null) {
            patient.setState(updateRequest.getState());
        }
        if (updateRequest.getZipCode() != null) {
            patient.setZipCode(updateRequest.getZipCode());
        }
        if (updateRequest.getCountry() != null) {
            patient.setCountry(updateRequest.getCountry());
        }
        if (updateRequest.getBloodType() != null) {
            patient.setBloodType(updateRequest.getBloodType());
        }
        if (updateRequest.getAllergies() != null) {
            patient.setAllergies(updateRequest.getAllergies());
        }
        if (updateRequest.getMedications() != null) {
            patient.setMedications(updateRequest.getMedications());
        }
        if (updateRequest.getMedicalConditions() != null) {
            patient.setMedicalConditions(updateRequest.getMedicalConditions());
        }
        if (updateRequest.getInsuranceProvider() != null) {
            patient.setInsuranceProvider(updateRequest.getInsuranceProvider());
        }
        if (updateRequest.getInsurancePolicyNumber() != null) {
            patient.setInsurancePolicyNumber(updateRequest.getInsurancePolicyNumber());
        }
    }
}