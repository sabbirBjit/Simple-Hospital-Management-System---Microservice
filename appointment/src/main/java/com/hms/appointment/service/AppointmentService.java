package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.model.Appointment;
import com.hms.appointment.model.AppointmentStatus;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public AppointmentResponse createAppointment(AppointmentRequest request, Long patientUserId) {
        // Validate doctor availability
        boolean isAvailable = availabilityRepository.findAvailabilityForTimeSlot(
            request.getDoctorUserId(),
            request.getAppointmentDate().getDayOfWeek(),
            request.getAppointmentTime(),
            request.getAppointmentTime().plusMinutes(request.getDurationMinutes()),
            LocalTime.of(0, 0)
        ).isPresent();
        
        if (!isAvailable) {
            throw new RuntimeException("Doctor is not available at the requested time");
        }
        
        // Check for conflicting appointments
        List<Appointment> conflicts = appointmentRepository.findAppointmentsByDoctorAndDate(
            request.getDoctorUserId(), request.getAppointmentDate());
        
        // Check for time conflicts
        boolean hasConflict = conflicts.stream().anyMatch(app -> {
            if (app.getStatus() == AppointmentStatus.CANCELLED) return false;
            
            LocalDateTime existingStart = app.getAppointmentDateTime();
            LocalDateTime existingEnd = app.getAppointmentEndTime();
            LocalDateTime newStart = request.getAppointmentDate().atTime(request.getAppointmentTime());
            LocalDateTime newEnd = newStart.plusMinutes(request.getDurationMinutes());
            
            return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        });
        
        if (hasConflict) {
            throw new RuntimeException("Time slot conflicts with existing appointment");
        }
        
        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientUserId(patientUserId);
        appointment.setDoctorUserId(request.getDoctorUserId());
        appointment.setPatientId(patientUserId);
        appointment.setDoctorId(request.getDoctorUserId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setAppointmentType(request.getAppointmentType());
        appointment.setReasonForVisit(request.getReasonForVisit());
        appointment.setNotes(request.getNotes());
        appointment.setCreatedBy(patientUserId);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Trigger Kafka event after successful appointment creation
        String appointmentEventData = String.format(
            "{\"appointmentId\":%d,\"patientUserId\":%d,\"doctorUserId\":%d,\"appointmentDate\":\"%s\",\"appointmentTime\":\"%s\",\"type\":\"%s\"}", 
            savedAppointment.getId(), 
            savedAppointment.getPatientUserId(), 
            savedAppointment.getDoctorUserId(),
            savedAppointment.getAppointmentDate().toString(),
            savedAppointment.getAppointmentTime().toString(),
            savedAppointment.getAppointmentType().toString()
        );
        
        kafkaTemplate.send("appointment.booked", appointmentEventData);
        
        return mapToResponse(savedAppointment);
    }
    
    public AppointmentResponse cancelAppointment(Long appointmentId, String reason, Long cancelledBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        
        Appointment appointment = optionalAppointment.get();
        
        if (!appointment.canBeCancelled()) {
            throw new RuntimeException("Appointment cannot be cancelled in current status");
        }
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason(reason);
        appointment.setCancelledBy(cancelledBy);
        appointment.setCancelledAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Trigger Kafka event for appointment cancellation
        String cancellationEventData = String.format(
            "{\"appointmentId\":%d,\"patientUserId\":%d,\"doctorUserId\":%d,\"cancellationReason\":\"%s\",\"cancelledBy\":%d,\"cancelledAt\":\"%s\"}", 
            appointment.getId(),
            appointment.getPatientUserId(),
            appointment.getDoctorUserId(),
            reason,
            cancelledBy,
            LocalDateTime.now().toString()
        );
        
        kafkaTemplate.send("appointment.cancelled", cancellationEventData);
        
        return mapToResponse(savedAppointment);
    }
    
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable, Long doctorUserId, 
                                                       Long patientUserId, String status, 
                                                       String fromDate, String toDate) {
        // Implementation for filtered appointment retrieval
        if (doctorUserId != null) {
            return appointmentRepository.findByDoctorUserId(doctorUserId, pageable)
                .map(this::mapToResponse);
        }
        if (patientUserId != null) {
            return appointmentRepository.findByPatientUserId(patientUserId, pageable)
                .map(this::mapToResponse);
        }
        return appointmentRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    public AppointmentResponse getAppointmentById(Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isEmpty()) {
            throw new RuntimeException("Appointment not found with id: " + id);
        }
        return mapToResponse(appointment.get());
    }
    
    public Page<AppointmentResponse> getPatientAppointments(Long patientUserId, Pageable pageable) {
        return appointmentRepository.findByPatientUserId(patientUserId, pageable)
            .map(this::mapToResponse);
    }
    
    public List<AppointmentResponse> getDoctorAppointments(Long doctorUserId, String date) {
        if (date != null) {
            LocalDate appointmentDate = LocalDate.parse(date);
            List<Appointment> appointments = appointmentRepository
                .findByDoctorUserIdAndAppointmentDateOrderByAppointmentTimeAsc(doctorUserId, appointmentDate);
            return appointments.stream().map(this::mapToResponse).collect(Collectors.toList());
        }
        return getDoctorAppointments(doctorUserId);
    }
    
    public List<AppointmentResponse> getDoctorAppointments(Long doctorUserId) {
        List<Appointment> appointments = appointmentRepository
            .findByDoctorUserIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorUserId);
        
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public AppointmentResponse updateAppointmentStatus(Long id, String status, String notes, Long updatedBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        
        Appointment appointment = optionalAppointment.get();
        
        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());
            appointment.setStatus(newStatus);
            
            if (notes != null && !notes.trim().isEmpty()) {
                appointment.setNotes(notes);
            }
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            // Trigger Kafka event for status change
            String statusEventData = String.format(
                "{\"appointmentId\":%d,\"status\":\"%s\",\"updatedBy\":%d,\"updatedAt\":\"%s\"}", 
                appointment.getId(),
                newStatus.toString(),
                updatedBy,
                LocalDateTime.now().toString()
            );
            
            kafkaTemplate.send("appointment.status.updated", statusEventData);
            
            return mapToResponse(savedAppointment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid appointment status: " + status);
        }
    }
    
    public AppointmentResponse rescheduleAppointment(Long id, LocalDate newDate, LocalTime newTime, 
                                                   String reason, Long rescheduledBy) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Appointment not found");
        }
        
        Appointment appointment = optionalAppointment.get();
        
        if (!appointment.canBeRescheduled()) {
            throw new RuntimeException("Appointment cannot be rescheduled in current status");
        }
        
        // Validate new time slot availability
        boolean isAvailable = availabilityRepository.findAvailabilityForTimeSlot(
            appointment.getDoctorUserId(),
            newDate.getDayOfWeek(),
            newTime,
            newTime.plusMinutes(appointment.getDurationMinutes()),
            LocalTime.of(0, 0)
        ).isPresent();
        
        if (!isAvailable) {
            throw new RuntimeException("Doctor is not available at the requested new time");
        }
        
        // Check for conflicts at new time
        List<Appointment> conflicts = appointmentRepository.findAppointmentsByDoctorAndDate(
            appointment.getDoctorUserId(), newDate);
        
        boolean hasConflict = conflicts.stream().anyMatch(app -> {
            if (app.getId().equals(id) || app.getStatus() == AppointmentStatus.CANCELLED) return false;
            
            LocalDateTime existingStart = app.getAppointmentDateTime();
            LocalDateTime existingEnd = app.getAppointmentEndTime();
            LocalDateTime newStart = newDate.atTime(newTime);
            LocalDateTime newEnd = newStart.plusMinutes(appointment.getDurationMinutes());
            
            return newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
        });
        
        if (hasConflict) {
            throw new RuntimeException("New time slot conflicts with existing appointment");
        }
        
        // Update appointment
        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);
        appointment.setNotes(appointment.getNotes() + "\nRescheduled: " + reason);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Trigger Kafka event for reschedule
        String rescheduleEventData = String.format(
            "{\"appointmentId\":%d,\"newDate\":\"%s\",\"newTime\":\"%s\",\"reason\":\"%s\",\"rescheduledBy\":%d}", 
            appointment.getId(),
            newDate.toString(),
            newTime.toString(),
            reason,
            rescheduledBy
        );
        
        kafkaTemplate.send("appointment.rescheduled", rescheduleEventData);
        
        return mapToResponse(savedAppointment);
    }
    
    public Map<String, Object> getAppointmentStatistics(String fromDate, String toDate, Long doctorUserId) {
        Map<String, Object> statistics = new HashMap<>();
        
        LocalDate startDate = fromDate != null ? LocalDate.parse(fromDate) : LocalDate.now().minusMonths(1);
        LocalDate endDate = toDate != null ? LocalDate.parse(toDate) : LocalDate.now();
        
        if (doctorUserId != null) {
            Long totalAppointments = appointmentRepository.countAppointmentsByDoctorAndDateRange(
                doctorUserId, startDate, endDate);
            statistics.put("totalAppointments", totalAppointments);
            statistics.put("doctorUserId", doctorUserId);
        } else {
            Long totalAppointments = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED) +
                                   appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED) +
                                   appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
            statistics.put("totalAppointments", totalAppointments);
        }
        
        statistics.put("scheduledCount", appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED));
        statistics.put("confirmedCount", appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED));
        statistics.put("completedCount", appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        statistics.put("cancelledCount", appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        statistics.put("noShowCount", appointmentRepository.countByStatus(AppointmentStatus.NO_SHOW));
        
        statistics.put("fromDate", startDate.toString());
        statistics.put("toDate", endDate.toString());
        
        return statistics;
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getPatientUserId(),
            appointment.getDoctorUserId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getDurationMinutes(),
            appointment.getStatus(),
            appointment.getAppointmentType(),
            appointment.getReasonForVisit(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt(),
            appointment.getCreatedBy()
        );
    }
}
