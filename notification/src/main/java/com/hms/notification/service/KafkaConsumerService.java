package com.hms.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaConsumerService {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @KafkaListener(topics = "user.created", groupId = "notification-service-group")
    public void handleUserCreated(String message) {
        try {
            logger.info("Received user.created event: {}", message);
            JsonNode userEvent = objectMapper.readTree(message);
            
            Long userId = userEvent.get("userId").asLong();
            String email = userEvent.get("email").asText();
            String firstName = userEvent.get("firstName").asText();
            String lastName = userEvent.get("lastName").asText();
            String username = userEvent.get("username").asText();
            String fullName = firstName + " " + lastName;
            
            // Send welcome email
            boolean emailSent = emailService.sendWelcomeEmail(email, fullName, username);
            
            if (emailSent) {
                // Create notification record
                NotificationRequest request = new NotificationRequest();
                request.setUserId(userId);
                request.setRecipientEmail(email);
                request.setRecipientName(fullName);
                request.setType(NotificationType.EMAIL);
                request.setSubject("Welcome to Hospital Management System");
                request.setContent("Welcome email sent successfully");
                request.setCategory("Welcome");
                request.setTemplateName("welcome");
                
                notificationService.createNotification(request);
                logger.info("Welcome notification created for user: {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("Error processing user.created event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "patient.registered", groupId = "notification-service-group")
    public void handlePatientRegistered(String message) {
        try {
            logger.info("Received patient.registered event: {}", message);
            JsonNode patientEvent = objectMapper.readTree(message);
            
            Long userId = patientEvent.get("userId").asLong();
            Long patientId = patientEvent.get("patientId").asLong();
            String email = patientEvent.get("email").asText();
            String firstName = patientEvent.get("firstName").asText();
            String lastName = patientEvent.get("lastName").asText();
            String fullName = firstName + " " + lastName;
            
            // Create patient registration notification
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setPatientId(patientId);
            request.setRecipientEmail(email);
            request.setRecipientName(fullName);
            request.setType(NotificationType.EMAIL);
            request.setSubject("Patient Profile Created");
            request.setContent("Your patient profile has been successfully created in our system.");
            request.setCategory("Patient Registration");
            
            notificationService.createNotification(request);
            logger.info("Patient registration notification created for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Error processing patient.registered event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "appointment.booked", groupId = "notification-service-group")
    public void handleAppointmentBooked(String message) {
        try {
            logger.info("Received appointment.booked event: {}", message);
            JsonNode appointmentEvent = objectMapper.readTree(message);
            
            Long appointmentId = appointmentEvent.get("appointmentId").asLong();
            Long patientUserId = appointmentEvent.get("patientUserId").asLong();
            Long doctorUserId = appointmentEvent.get("doctorUserId").asLong();
            String appointmentDate = appointmentEvent.get("appointmentDate").asText();
            String appointmentTime = appointmentEvent.get("appointmentTime").asText();
            String appointmentType = appointmentEvent.get("type").asText();
            
            // You would typically fetch patient and doctor details from their respective services
            // For now, we'll create a basic confirmation notification
            
            NotificationRequest request = new NotificationRequest();
            request.setUserId(patientUserId);
            request.setAppointmentId(appointmentId);
            request.setPatientId(patientUserId);
            request.setDoctorId(doctorUserId);
            request.setType(NotificationType.EMAIL);
            request.setSubject("Appointment Confirmation");
            request.setContent(String.format(
                "Your %s appointment has been confirmed for %s at %s.",
                appointmentType, appointmentDate, appointmentTime
            ));
            request.setCategory("Appointment Confirmation");
            request.setTemplateName("appointment-confirmation");
            
            notificationService.createNotification(request);
            logger.info("Appointment confirmation notification created for appointment: {}", appointmentId);
            
        } catch (Exception e) {
            logger.error("Error processing appointment.booked event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "appointment.cancelled", groupId = "notification-service-group")
    public void handleAppointmentCancelled(String message) {
        try {
            logger.info("Received appointment.cancelled event: {}", message);
            JsonNode appointmentEvent = objectMapper.readTree(message);
            
            Long appointmentId = appointmentEvent.get("appointmentId").asLong();
            Long patientUserId = appointmentEvent.get("patientUserId").asLong();
            Long doctorUserId = appointmentEvent.get("doctorUserId").asLong();
            String cancellationReason = appointmentEvent.get("cancellationReason").asText();
            
            NotificationRequest request = new NotificationRequest();
            request.setUserId(patientUserId);
            request.setAppointmentId(appointmentId);
            request.setPatientId(patientUserId);
            request.setDoctorId(doctorUserId);
            request.setType(NotificationType.EMAIL);
            request.setSubject("Appointment Cancelled");
            request.setContent(String.format(
                "Your appointment has been cancelled. Reason: %s",
                cancellationReason
            ));
            request.setCategory("Appointment Cancellation");
            
            notificationService.createNotification(request);
            logger.info("Appointment cancellation notification created for appointment: {}", appointmentId);
            
        } catch (Exception e) {
            logger.error("Error processing appointment.cancelled event: {}", e.getMessage(), e);
        }
    }
    
    @KafkaListener(topics = "appointment.reminder", groupId = "notification-service-group")
    public void handleAppointmentReminder(String message) {
        try {
            logger.info("Received appointment.reminder event: {}", message);
            JsonNode reminderEvent = objectMapper.readTree(message);
            
            Long appointmentId = reminderEvent.get("appointmentId").asLong();
            Long patientUserId = reminderEvent.get("patientUserId").asLong();
            Long doctorUserId = reminderEvent.get("doctorUserId").asLong();
            String appointmentDate = reminderEvent.get("appointmentDate").asText();
            String appointmentTime = reminderEvent.get("appointmentTime").asText();
            String reminderType = reminderEvent.get("reminderType").asText();
            
            NotificationRequest request = new NotificationRequest();
            request.setUserId(patientUserId);
            request.setAppointmentId(appointmentId);
            request.setPatientId(patientUserId);
            request.setDoctorId(doctorUserId);
            request.setType(NotificationType.EMAIL);
            request.setSubject("Appointment Reminder");
            request.setContent(String.format(
                "Reminder: You have an appointment scheduled for %s at %s.",
                appointmentDate, appointmentTime
            ));
            request.setCategory("Appointment Reminder");
            request.setTemplateName("appointment-reminder");
            request.setScheduledAt(LocalDateTime.now()); // Send immediately
            
            notificationService.createNotification(request);
            logger.info("Appointment reminder notification created for appointment: {}", appointmentId);
            
        } catch (Exception e) {
            logger.error("Error processing appointment.reminder event: {}", e.getMessage(), e);
        }
    }
}
