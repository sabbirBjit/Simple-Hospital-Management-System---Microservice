package com.hms.appointment.repository;

import com.hms.appointment.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    
    // Find availability by doctor
    List<DoctorAvailability> findByDoctorUserIdOrderByDayOfWeekAscStartTimeAsc(Long doctorUserId);
    
    // Find availability by doctor and day
    Optional<DoctorAvailability> findByDoctorUserIdAndDayOfWeek(Long doctorUserId, DayOfWeek dayOfWeek);
    
    // Find available slots by doctor
    List<DoctorAvailability> findByDoctorUserIdAndIsAvailableTrueOrderByDayOfWeekAscStartTimeAsc(Long doctorUserId);
    
    // Find availability by doctor and day (available only)
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctorUserId = :doctorUserId " +
           "AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true " +
           "AND da.startTime != :unavailableTime AND da.endTime != :unavailableTime")
    Optional<DoctorAvailability> findByDoctorUserIdAndDayOfWeekAndIsAvailableTrue(
        @Param("doctorUserId") Long doctorUserId, 
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("unavailableTime") LocalTime unavailableTime);
    
    // Check if doctor is available at specific time
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctorUserId = :doctorUserId " +
           "AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true " +
           "AND da.startTime != :unavailableTime AND da.endTime != :unavailableTime " +
           "AND da.startTime <= :time AND da.endTime > :time")
    Optional<DoctorAvailability> findAvailabilityForTime(
        @Param("doctorUserId") Long doctorUserId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("time") LocalTime time,
        @Param("unavailableTime") LocalTime unavailableTime
    );
    
    // Check if time slot is available for duration
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctorUserId = :doctorUserId " +
           "AND da.dayOfWeek = :dayOfWeek AND da.isAvailable = true " +
           "AND da.startTime != :unavailableTime AND da.endTime != :unavailableTime " +
           "AND da.startTime <= :startTime AND da.endTime >= :endTime")
    Optional<DoctorAvailability> findAvailabilityForTimeSlot(
        @Param("doctorUserId") Long doctorUserId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("unavailableTime") LocalTime unavailableTime
    );
    
    // Find all available doctors for a specific day and time
    @Query("SELECT da FROM DoctorAvailability da WHERE da.dayOfWeek = :dayOfWeek " +
           "AND da.isAvailable = true AND da.startTime <= :time AND da.endTime > :time")
    List<DoctorAvailability> findAvailableDoctorsForTime(
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("time") LocalTime time
    );
    
    // Check if doctor has any availability
    Boolean existsByDoctorUserIdAndIsAvailableTrue(Long doctorUserId);
    
    // Count availability slots by doctor
    Long countByDoctorUserId(Long doctorUserId);
    
    // Count available slots by doctor
    Long countByDoctorUserIdAndIsAvailableTrue(Long doctorUserId);
    
    // Find doctors with availability on specific day
    @Query("SELECT DISTINCT da.doctorUserId FROM DoctorAvailability da " +
           "WHERE da.dayOfWeek = :dayOfWeek AND da.isAvailable = true")
    List<Long> findDoctorUserIdsAvailableOnDay(@Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    // Delete by doctor and day (for updating availability)
    void deleteByDoctorUserIdAndDayOfWeek(Long doctorUserId, DayOfWeek dayOfWeek);
    
    // Find overlapping availability (for validation) - handle unavailable days
    @Query("SELECT da FROM DoctorAvailability da WHERE da.doctorUserId = :doctorUserId " +
           "AND da.dayOfWeek = :dayOfWeek AND da.id != :excludeId AND da.isAvailable = true " +
           "AND da.startTime != :unavailableTime AND da.endTime != :unavailableTime " +
           "AND ((da.startTime <= :startTime AND :startTime < da.endTime) " +
           "OR (:startTime <= da.startTime AND da.startTime < :endTime))")
    List<DoctorAvailability> findOverlappingAvailability(
        @Param("doctorUserId") Long doctorUserId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") Long excludeId,
        @Param("unavailableTime") LocalTime unavailableTime
    );
}
