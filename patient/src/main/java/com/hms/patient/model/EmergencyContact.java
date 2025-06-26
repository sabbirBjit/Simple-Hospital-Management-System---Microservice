package com.hms.patient.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String relationship;
    
    @NotBlank
    @Size(max = 15)
    @Column(nullable = false)
    private String phoneNumber;
    
    @Size(max = 100)
    private String email;
    
    @Size(max = 200)
    private String address;
    
    @Column(nullable = false)
    private Boolean isPrimary = false;
}
