package com.hms.notification.dto;

import com.hms.notification.model.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String recipientEmail;
    
    private String recipientName;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    private String content;
    private String htmlContent;
    
    private LocalDateTime scheduledAt;
    
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    
    private String category;
    private String templateName;
    
    private Map<String, Object> templateVariables;
}
