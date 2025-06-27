package com.hms.appointment.repository;

import com.hms.appointment.model.Appointment;
import com.hms.appointment.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find appointments by patient
    List<Appointment> findByPatientUserIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientUserId);
    
    Page<Appointment> findByPatientUserId(Long patientUserId, Pageable pageable);
    
    // Find appointments by doctor
    List<Appointment> findByDoctorUserIdOrderByAppointmentDateAscAppointmentTimeAsc(Long doctorUserId);
    
    Page<Appointment> findByDoctorUserId(Long doctorUserId, Pageable pageable);
    
    // Find appointments by date
    List<Appointment> findByAppointmentDateOrderByAppointmentTimeAsc(LocalDate appointmentDate);
    
    // Find appointments by date range
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAscAppointmentTimeAsc(
        LocalDate startDate, LocalDate endDate);
    
    // Find appointments by status
    List<Appointment> findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus status);
    
    // Find appointments by doctor and date
    List<Appointment> findByDoctorUserIdAndAppointmentDateOrderByAppointmentTimeAsc(
        Long doctorUserId, LocalDate appointmentDate);
    
    // Find appointments by patient and date range
    List<Appointment> findByPatientUserIdAndAppointmentDateBetween(
        Long patientUserId, LocalDate startDate, LocalDate endDate);
    
    // Find appointments by doctor and date range
    List<Appointment> findByDoctorUserIdAndAppointmentDateBetween(
        Long doctorUserId, LocalDate startDate, LocalDate endDate);
    
    // Check for conflicts (overlapping appointments) - Fixed JPQL syntax
    @Query("SELECT a FROM Appointment a WHERE a.doctorUserId = :doctorUserId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND ((a.appointmentTime < :endTime) AND (:startTime < function('ADDTIME', a.appointmentTime, function('SEC_TO_TIME', a.durationMinutes * 60))))")
    List<Appointment> findConflictingAppointments(
        @Param("doctorUserId") Long doctorUserId,
        @Param("appointmentDate") LocalDate appointmentDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    // Alternative simpler conflict check - using service layer logic instead
    @Query("SELECT a FROM Appointment a WHERE a.doctorUserId = :doctorUserId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.status IN ('SCHEDULED', 'CONFIRMED')")
    List<Appointment> findAppointmentsByDoctorAndDate(
        @Param("doctorUserId") Long doctorUserId,
        @Param("appointmentDate") LocalDate appointmentDate
    );
    
    // Find upcoming appointments for reminders - Fixed JPQL syntax
    @Query("SELECT a FROM Appointment a WHERE a.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND a.appointmentDate = :date " +
           "AND a.appointmentTime BETWEEN :startTime AND :endTime")
    List<Appointment> findUpcomingAppointmentsForDate(
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    // Find today's appointments
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :today " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointments(@Param("today") LocalDate today);
    
    // Find appointments by multiple statuses
    List<Appointment> findByStatusInOrderByAppointmentDateAscAppointmentTimeAsc(List<AppointmentStatus> statuses);
    
    // Count appointments by doctor and date
    Long countByDoctorUserIdAndAppointmentDate(Long doctorUserId, LocalDate appointmentDate);
    
    // Count appointments by patient
    Long countByPatientUserId(Long patientUserId);
    
    // Count appointments by status
    Long countByStatus(AppointmentStatus status);
    
    // Find appointments by ID and patient (for patient access control)
    Optional<Appointment> findByIdAndPatientUserId(Long id, Long patientUserId);
    
    // Find appointments by ID and doctor (for doctor access control)
    Optional<Appointment> findByIdAndDoctorUserId(Long id, Long doctorUserId);
    
    // Find active appointments (not cancelled)
    @Query("SELECT a FROM Appointment a WHERE a.status != 'CANCELLED' " +
           "ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findActiveAppointments();
    
    // Statistics queries
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    Long countAppointmentsByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorUserId = :doctorUserId " +
           "AND a.appointmentDate BETWEEN :startDate AND :endDate")
    Long countAppointmentsByDoctorAndDateRange(
        @Param("doctorUserId") Long doctorUserId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
