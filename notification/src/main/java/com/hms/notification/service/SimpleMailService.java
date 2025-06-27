package com.hms.notification.service;

import com.hms.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SimpleMailService {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleMailService.class);
    
    private final JavaMailSender javaMailSender;
    
    @Value("${mailtrap.from.email}")
    private String fromEmail;
    
    @Value("${mailtrap.from.name}")
    private String fromName;
    
    public SimpleMailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    public boolean sendEmail(Notification notification) {
        try {
            logger.info("Sending email to: {} with subject: {}", 
                       notification.getRecipientEmail(), notification.getSubject());
            
            if (notification.getHtmlContent() != null && !notification.getHtmlContent().trim().isEmpty()) {
                return sendHtmlEmail(
                    notification.getRecipientEmail(), 
                    notification.getRecipientName(),
                    notification.getSubject(), 
                    notification.getHtmlContent(),
                    notification.getContent()
                );
            } else {
                return sendTextEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getContent()
                );
            }
            
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", notification.getRecipientEmail(), e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendTextEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            javaMailSender.send(message);
            logger.info("Text email sent successfully to {}", to);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send text email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendHtmlEmail(String to, String name, String subject, String htmlContent, String textContent) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            // helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            
            if (htmlContent != null && !htmlContent.trim().isEmpty()) {
                helper.setText(textContent != null ? textContent : "", true);
            } else {
                helper.setText(textContent != null ? textContent : "");
            }
            
            javaMailSender.send(mimeMessage);
            logger.info("HTML email sent successfully to {}", to);
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
