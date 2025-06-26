package com.hms.patient.dto;

import com.hms.patient.model.BloodType;
import com.hms.patient.model.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Please provide a valid phone number")
    private String phoneNumber;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;
    
    @Size(max = 10, message = "ZIP code must not exceed 10 characters")
    private String zipCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
    
    private BloodType bloodType;
    
    @Size(max = 1000, message = "Allergies must not exceed 1000 characters")
    private String allergies;
    
    @Size(max = 1000, message = "Medications must not exceed 1000 characters")
    private String medications;
    
    @Size(max = 1000, message = "Medical conditions must not exceed 1000 characters")
    private String medicalConditions;
    
    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    private String insuranceProvider;
    
    @Size(max = 50, message = "Insurance policy number must not exceed 50 characters")
    private String insurancePolicyNumber;

    public void setUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        this.userId = userId;
    }
}
