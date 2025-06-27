package com.hms.notification.controller;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResponse;
import com.hms.notification.model.NotificationType;
import com.hms.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        try {
            NotificationResponse response = notificationService.createNotification(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        try {
            List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Page<NotificationResponse>> getUserNotificationsByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationResponse> notifications = notificationService.getUserNotificationsByType(userId, type, pageable);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<NotificationResponse>> getAppointmentNotifications(@PathVariable Long appointmentId) {
        try {
            List<NotificationResponse> notifications = notificationService.getAppointmentNotifications(appointmentId);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        try {
            Optional<NotificationResponse> notification = notificationService.getNotificationById(id);
            return notification.map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        try {
            notificationService.retryFailedNotifications();
            return new ResponseEntity<>("Failed notifications retry initiated", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrying failed notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/process-scheduled")
    public ResponseEntity<String> processScheduledNotifications() {
        try {
            notificationService.processScheduledNotifications();
            return new ResponseEntity<>("Scheduled notifications processing initiated", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error processing scheduled notifications", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
