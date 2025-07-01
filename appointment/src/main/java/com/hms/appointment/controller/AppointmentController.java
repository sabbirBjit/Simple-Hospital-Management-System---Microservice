package com.hms.appointment.controller;

import com.hms.appointment.dto.AppointmentRequest;
import com.hms.appointment.dto.AppointmentResponse;
import com.hms.appointment.dto.DoctorAvailabilityRequest;
import com.hms.appointment.dto.DoctorAvailabilityResponse;
import com.hms.appointment.service.AppointmentService;
import com.hms.appointment.service.DoctorAvailabilityService;
import com.hms.appointment.util.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

// import Logger 
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorAvailabilityService availabilityService;
    
    @Autowired
    private JwtUtil jwtUtil;

    // Create new appointment
    @PostMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('ADMIN')")
    public ResponseEntity<?> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long patientUserId) {
        try {
            if (patientUserId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            AppointmentResponse appointment = appointmentService.createAppointment(request, patientUserId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment created successfully",
                "data", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to create appointment: " + e.getMessage()
            ));
        }
    }

    // Get all appointments (admin/doctor view)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE')")
    public ResponseEntity<?> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long doctorUserId,
            @RequestParam(required = false) Long patientUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(
                pageable, doctorUserId, patientUserId, status, fromDate, toDate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointments retrieved successfully",
                "data", appointments.getContent(),
                "pagination", Map.of(
                    "page", appointments.getNumber(),
                    "size", appointments.getSize(),
                    "totalElements", appointments.getTotalElements(),
                    "totalPages", appointments.getTotalPages(),
                    "hasNext", appointments.hasNext(),
                    "hasPrevious", appointments.hasPrevious()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve appointments: " + e.getMessage()
            ));
        }
    }

    // Get appointment by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('NURSE') or hasRole('PATIENT')")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            AppointmentResponse appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment found",
                "data", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve appointment: " + e.getMessage()
            ));
        }
    }

    // Get patient appointments
    @GetMapping("/patient")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> getPatientAppointments(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long patientUserId = jwtUtil.getUserIdFromToken(authorizationHeader);
            if (patientUserId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in JWT token"));
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientUserId, pageable);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Patient appointments retrieved successfully",
                "data", appointments.getContent(),
                "pagination", Map.of(
                    "page", appointments.getNumber(),
                    "size", appointments.getSize(),
                    "totalElements", appointments.getTotalElements(),
                    "totalPages", appointments.getTotalPages()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve patient appointments: " + e.getMessage()
            ));
        }
    }

    // Get doctor appointments
    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> getDoctorAppointments(
            @RequestHeader(value = "X-User-Id", required = false) Long doctorUserId,
            @RequestParam(required = false) String date) {
        try {
            if (doctorUserId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorUserId, date);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Doctor appointments retrieved successfully",
                "data", appointments
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve doctor appointments: " + e.getMessage()
            ));
        }
    }

    // Update appointment status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) Long updatedBy) {
        try {
            if (updatedBy == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            String status = request.get("status");
            String notes = request.get("notes");
            
            AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, status, notes, updatedBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment status updated successfully",
                "data", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to update appointment status: " + e.getMessage()
            ));
        }
    }

    // Reschedule appointment
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) Long rescheduledBy) {
        try {
            if (rescheduledBy == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            LocalDate newDate = LocalDate.parse(request.get("newAppointmentDate"));
            LocalTime newTime = LocalTime.parse(request.get("newAppointmentTime"));
            String reason = request.get("rescheduleReason");
            
            AppointmentResponse appointment = appointmentService.rescheduleAppointment(id, newDate, newTime, reason, rescheduledBy);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment rescheduled successfully",
                "data", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to reschedule appointment: " + e.getMessage()
            ));
        }
    }

    // Cancel appointment
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) Long cancelledBy) {
        try {
            if (cancelledBy == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            String reason = request != null ? request.get("cancellationReason") : "No reason provided";
            AppointmentResponse appointment = appointmentService.cancelAppointment(id, reason, cancelledBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment cancelled successfully",
                "data", appointment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to cancel appointment: " + e.getMessage()
            ));
        }
    }

    // Doctor availability endpoints
    @PostMapping("/availability")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> setDoctorAvailability(
            @Valid @RequestBody DoctorAvailabilityRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long doctorUserId) {
        try {
            if (doctorUserId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID not found in request"));
            }
            
            // Handle weekly schedule setup - validate individual schedules differently
            if (request.getWeeklySchedule() != null && !request.getWeeklySchedule().isEmpty()) {
                // For weekly schedule, validate each individual request
                for (DoctorAvailabilityRequest weeklyRequest : request.getWeeklySchedule()) {
                    if (weeklyRequest.getDayOfWeek() == null) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "Day of week is required for each schedule item"
                        ));
                    }
                    if (weeklyRequest.getIsAvailable() && 
                        (weeklyRequest.getStartTime() == null || weeklyRequest.getEndTime() == null)) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "Start time and end time are required when available is true"
                        ));
                    }
                }
                
                List<DoctorAvailabilityResponse> responses = availabilityService.setWeeklySchedule(
                    request.getDoctorUserId() != null ? request.getDoctorUserId() : doctorUserId, 
                    request.getWeeklySchedule()
                );
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Weekly schedule set successfully",
                    "data", responses
                ));
            } else {
                // Handle single day availability - requires validation
                if (request.getDayOfWeek() == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Day of week is required for single availability"
                    ));
                }
                
                DoctorAvailabilityResponse availability = availabilityService.setAvailability(doctorUserId, request);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Doctor availability set successfully",
                    "data", availability
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to set doctor availability: " + e.getMessage()
            ));
        }
    }

    // Statistics endpoint
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<?> getAppointmentStatistics(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long doctorUserId) {
        try {
            Map<String, Object> statistics = appointmentService.getAppointmentStatistics(fromDate, toDate, doctorUserId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Appointment statistics retrieved successfully",
                "data", statistics
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve appointment statistics: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/availability/slots")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorUserId,
            @RequestParam String date,
            @RequestParam int duration) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<LocalTime> slots = availabilityService.getAvailableTimeSlots(doctorUserId, localDate, duration);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Available slots retrieved successfully",
                "data", slots
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to retrieve available slots: " + e.getMessage()
            ));
        }
    }

}























