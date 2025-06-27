package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.model.Appointment;
import com.hms.appointment.model.AppointmentStatus;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
            request.getAppointmentTime().plusMinutes(request.getDurationMinutes())
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
    
    public List<AppointmentResponse> getPatientAppointments(Long patientUserId) {
        List<Appointment> appointments = appointmentRepository
            .findByPatientUserIdOrderByAppointmentDateDescAppointmentTimeDesc(patientUserId);
        
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<AppointmentResponse> getDoctorAppointments(Long doctorUserId) {
        List<Appointment> appointments = appointmentRepository
            .findByDoctorUserIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorUserId);
        
        return appointments.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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
