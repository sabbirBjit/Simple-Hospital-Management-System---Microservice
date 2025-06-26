package com.hms.patient.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private Long userId; // Reference to auth service user
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String firstName;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String lastName;
    
    @Email
    @Size(max = 100)
    @Column(nullable = false)
    private String email;
    
    @Size(max = 15)
    private String phoneNumber;
    
    @Past
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Size(max = 500)
    private String address;
    
    @Size(max = 50)
    private String city;
    
    @Size(max = 50)
    private String state;
    
    @Size(max = 10)
    private String zipCode;
    
    @Size(max = 50)
    private String country;
    
    @Enumerated(EnumType.STRING)
    private BloodType bloodType;
    
    @Size(max = 1000)
    private String allergies;
    
    @Size(max = 1000)
    private String medications;
    
    @Size(max = 1000)
    private String medicalConditions;
    
    @Size(max = 100)
    private String insuranceProvider;
    
    @Size(max = 50)
    private String insurancePolicyNumber;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistory = new ArrayList<>();
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    
    // Calculated field - improved age calculation
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        LocalDate now = LocalDate.now();
        int age = now.getYear() - dateOfBirth.getYear();
        
        // Adjust if birthday hasn't occurred this year
        if (now.getDayOfYear() < dateOfBirth.getDayOfYear()) {
            age--;
        }
        
        return Math.max(0, age);
    }
    
    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Helper method to check if profile is complete
    public boolean isProfileComplete() {
        return firstName != null && lastName != null && email != null && 
               dateOfBirth != null && gender != null && phoneNumber != null;
    }
}
