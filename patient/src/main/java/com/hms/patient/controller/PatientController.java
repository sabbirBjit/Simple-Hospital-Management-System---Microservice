package com.hms.patient.controller;

import com.hms.patient.dto.ApiResponse;
import com.hms.patient.dto.PatientRequest;
import com.hms.patient.dto.PatientResponse;
import com.hms.patient.dto.PatientProfileUpdateRequest;
import com.hms.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    
    @Autowired
    private PatientService patientService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<?> createPatient(@Valid @RequestBody PatientRequest patientRequest) {
        try {
            PatientResponse patient = patientService.createPatient(patientRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Patient created successfully", patient));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create patient: " + e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getAllPatients() {
        try {
            List<PatientResponse> patients = patientService.getAllPatients();
            return ResponseEntity.ok(new ApiResponse(true, "Patients retrieved successfully", patients));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve patients: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getPatientById(@PathVariable Long id) {
        try {
            Optional<PatientResponse> patient = patientService.getPatientById(id);
            if (patient.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Patient found", patient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve patient: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or @patientService.getPatientByUserId(#userId).orElse(null)?.userId == authentication.principal?.id")
    public ResponseEntity<?> getPatientByUserId(@PathVariable Long userId) {
        try {
            Optional<PatientResponse> patient = patientService.getPatientByUserId(userId);
            if (patient.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Patient found", patient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve patient: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> searchPatientsByName(@RequestParam String name) {
        try {
            List<PatientResponse> patients = patientService.searchPatientsByName(name);
            return ResponseEntity.ok(new ApiResponse(true, "Patients found", patients));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to search patients: " + e.getMessage()));
        }
    }
    
    @GetMapping("/city/{city}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getPatientsByCity(@PathVariable String city) {
        try {
            List<PatientResponse> patients = patientService.getPatientsByCity(city);
            return ResponseEntity.ok(new ApiResponse(true, "Patients found for city: " + city, patients));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve patients by city: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NURSE') or @patientService.getPatientById(#id).orElse(null)?.userId == authentication.principal?.id")
    public ResponseEntity<?> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRequest patientRequest) {
        try {
            PatientResponse updatedPatient = patientService.updatePatient(id, patientRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Patient updated successfully", updatedPatient));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update patient: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.ok(new ApiResponse(true, "Patient deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete patient: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/update")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody PatientProfileUpdateRequest updateRequest,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "User ID not found in request headers"));
            }
            
            PatientResponse updatedPatient = patientService.updatePatientProfileByUserId(userId, updateRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedPatient));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update profile: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/full-update")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> updateMyFullProfile(@Valid @RequestBody PatientRequest patientRequest,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "User ID not found in request headers"));
            }
            
            // Set the userId from header to ensure consistency
            patientRequest.setUserId(userId);
            
            PatientResponse updatedPatient = patientService.updatePatientProfileByUserId(userId, patientRequest);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedPatient));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update profile: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "User ID not found in request headers"));
            }
            
            Optional<PatientResponse> patient = patientService.getPatientByUserId(userId);
            if (patient.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved", patient.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve profile: " + e.getMessage()));
        }
    }
}
