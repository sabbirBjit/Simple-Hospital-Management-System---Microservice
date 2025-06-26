package com.hms.patient.repository;

import com.hms.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByUserId(Long userId);
    
    Optional<Patient> findByEmail(String email);
    
    Boolean existsByUserId(Long userId);
    
    Boolean existsByEmail(String email);
    
    List<Patient> findByIsActive(Boolean isActive);
    
    @Query("SELECT p FROM Patient p WHERE p.firstName LIKE %:name% OR p.lastName LIKE %:name%")
    List<Patient> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Patient p WHERE p.phoneNumber = :phone")
    Optional<Patient> findByPhoneNumber(@Param("phone") String phoneNumber);
    
    List<Patient> findByCity(String city);
    
    List<Patient> findByState(String state);
}
