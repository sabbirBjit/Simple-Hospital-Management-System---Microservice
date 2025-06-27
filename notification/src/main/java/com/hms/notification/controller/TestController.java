package com.hms.notification.controller;

import com.hms.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send-welcome-email")
    public ResponseEntity<String> testWelcomeEmail(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String username) {
        
        try {
            boolean sent = emailService.sendWelcomeEmail(email, name, username);
            if (sent) {
                return ResponseEntity.ok("Welcome email sent successfully to " + email);
            } else {
                return ResponseEntity.badRequest().body("Failed to send welcome email");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-appointment-confirmation")
    public ResponseEntity<String> testAppointmentConfirmation(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String doctor) {
        
        try {
            boolean sent = emailService.sendAppointmentConfirmation(email, name, date, time, doctor);
            if (sent) {
                return ResponseEntity.ok("Appointment confirmation email sent successfully to " + email);
            } else {
                return ResponseEntity.badRequest().body("Failed to send appointment confirmation email");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-reminder")
    public ResponseEntity<String> testAppointmentReminder(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String doctor) {
        
        try {
            boolean sent = emailService.sendAppointmentReminder(email, name, date, time, doctor);
            if (sent) {
                return ResponseEntity.ok("Appointment reminder email sent successfully to " + email);
            } else {
                return ResponseEntity.badRequest().body("Failed to send appointment reminder email");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
