package com.hms.notification.dto;

import com.hms.notification.model.NotificationStatus;
import com.hms.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    
    private Long id;
    private Long userId;
    private String recipientEmail;
    private String recipientName;
    private NotificationType type;
    private String subject;
    private String content;
    private NotificationStatus status;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private String category;
    private String templateName;
}
