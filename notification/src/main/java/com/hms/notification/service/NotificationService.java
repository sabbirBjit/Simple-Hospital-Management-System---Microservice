package com.hms.notification.service;

import com.hms.notification.dto.NotificationRequest;
import com.hms.notification.dto.NotificationResponse;
import com.hms.notification.model.Notification;
import com.hms.notification.model.NotificationStatus;
import com.hms.notification.model.NotificationType;
import com.hms.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpleMailService simpleMailService;
    
    public NotificationResponse createNotification(NotificationRequest request) {
        logger.info("Creating notification for user: {}", request.getUserId());
        
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setRecipientEmail(request.getRecipientEmail());
        notification.setRecipientName(request.getRecipientName());
        notification.setType(request.getType());
        notification.setSubject(request.getSubject());
        notification.setContent(request.getContent());
        notification.setHtmlContent(request.getHtmlContent());
        notification.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now());
        notification.setAppointmentId(request.getAppointmentId());
        notification.setPatientId(request.getPatientId());
        notification.setDoctorId(request.getDoctorId());
        notification.setCategory(request.getCategory());
        notification.setTemplateName(request.getTemplateName());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send immediately if scheduled for now or past
        if (savedNotification.getScheduledAt().isBefore(LocalDateTime.now()) || 
            savedNotification.getScheduledAt().isEqual(LocalDateTime.now())) {
            sendNotificationAsync(savedNotification.getId());
        }
        
        return mapToResponse(savedNotification);
    }
    
    @Async
    public void sendNotificationAsync(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            logger.error("Notification not found with ID: {}", notificationId);
            return;
        }
        
        Notification notification = optionalNotification.get();
        sendNotification(notification);
    }
    
    public boolean sendNotification(Notification notification) {
        logger.info("Sending notification ID: {} to: {}", notification.getId(), notification.getRecipientEmail());
        
        try {
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);
            
            boolean success = false;
            
            switch (notification.getType()) {
                case EMAIL:
                    success = simpleMailService.sendEmail(notification);
                    break;
                case SMS:
                    // Future SMS implementation
                    logger.warn("SMS notifications not yet implemented");
                    break;
                case PUSH_NOTIFICATION:
                    // Future push notification implementation
                    logger.warn("Push notifications not yet implemented");
                    break;
                case IN_APP:
                    // Future in-app notification implementation
                    logger.warn("In-app notifications not yet implemented");
                    break;
            }
            
            if (success) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                logger.info("Notification sent successfully: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage("Failed to send notification");
                logger.error("Failed to send notification: {}", notification.getId());
            }
            
            notificationRepository.save(notification);
            return success;
            
        } catch (Exception e) {
            logger.error("Error sending notification {}: {}", notification.getId(), e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            return false;
        }
    }
    
    public List<NotificationResponse> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public Page<NotificationResponse> getUserNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(this::mapToResponse);
    }
    
    public List<NotificationResponse> getAppointmentNotifications(Long appointmentId) {
        List<Notification> notifications = notificationRepository.findByAppointmentIdOrderByCreatedAtDesc(appointmentId);
        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public Optional<NotificationResponse> getNotificationById(Long id) {
        return notificationRepository.findById(id)
            .map(this::mapToResponse);
    }
    
    public void retryFailedNotifications() {
        logger.info("Retrying failed notifications...");
        
        List<Notification> failedNotifications = notificationRepository
            .findByStatusAndRetryCountLessThanAndScheduledAtBefore(
                NotificationStatus.FAILED, 3, LocalDateTime.now());
        
        for (Notification notification : failedNotifications) {
            logger.info("Retrying notification: {}", notification.getId());
            notification.setStatus(NotificationStatus.RETRYING);
            notificationRepository.save(notification);
            sendNotificationAsync(notification.getId());
        }
    }
    
    public void processScheduledNotifications() {
        logger.info("Processing scheduled notifications...");
        
        List<Notification> scheduledNotifications = notificationRepository
            .findByStatusAndRetryCountLessThanAndScheduledAtBefore(
                NotificationStatus.PENDING, 3, LocalDateTime.now());
        
        for (Notification notification : scheduledNotifications) {
            logger.info("Processing scheduled notification: {}", notification.getId());
            sendNotificationAsync(notification.getId());
        }
    }
    
    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getRecipientEmail(),
            notification.getRecipientName(),
            notification.getType(),
            notification.getSubject(),
            notification.getContent(),
            notification.getStatus(),
            notification.getErrorMessage(),
            notification.getRetryCount(),
            notification.getScheduledAt(),
            notification.getSentAt(),
            notification.getCreatedAt(),
            notification.getUpdatedAt(),
            notification.getAppointmentId(),
            notification.getPatientId(),
            notification.getDoctorId(),
            notification.getCategory(),
            notification.getTemplateName()
        );
    }
}
