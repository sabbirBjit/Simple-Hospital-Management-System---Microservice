package com.hms.patient.dto;

import com.hms.patient.model.BloodType;
import com.hms.patient.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private int age;
    private Gender gender;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private BloodType bloodType;
    private String allergies;
    private String medications;
    private String medicalConditions;
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
