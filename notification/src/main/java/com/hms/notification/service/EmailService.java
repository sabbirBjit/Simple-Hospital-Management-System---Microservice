package com.hms.notification.service;

import com.hms.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private SimpleMailService simpleMailService;
    
    @Value("${mailtrap.from.email}")
    private String fromEmail;
    
    @Value("${mailtrap.from.name}")
    private String fromName;
    
    public boolean sendEmail(Notification notification) {
        try {
            logger.info("Sending email to: {} with subject: {}", 
                       notification.getRecipientEmail(), notification.getSubject());
            
            return simpleMailService.sendEmail(notification);
            
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", notification.getRecipientEmail(), e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendWelcomeEmail(String recipientEmail, String recipientName, String username) {
        try {
            String subject = "Welcome to Hospital Management System";
            String htmlContent = buildWelcomeEmailContent(recipientName, username);
            String textContent = buildWelcomeEmailTextContent(recipientName, username);
            
            return simpleMailService.sendHtmlEmail(recipientEmail, recipientName, subject, htmlContent, textContent);
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendAppointmentConfirmation(String recipientEmail, String recipientName, 
                                             String appointmentDate, String appointmentTime, 
                                             String doctorName) {
        try {
            String subject = "Appointment Confirmation - Hospital Management System";
            String htmlContent = buildAppointmentConfirmationContent(recipientName, appointmentDate, appointmentTime, doctorName);
            String textContent = buildAppointmentConfirmationTextContent(recipientName, appointmentDate, appointmentTime, doctorName);
            
            return simpleMailService.sendHtmlEmail(recipientEmail, recipientName, subject, htmlContent, textContent);
            
        } catch (Exception e) {
            logger.error("Failed to send appointment confirmation email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendAppointmentReminder(String recipientEmail, String recipientName, 
                                         String appointmentDate, String appointmentTime, 
                                         String doctorName) {
        try {
            String subject = "Appointment Reminder - Tomorrow";
            String htmlContent = buildAppointmentReminderContent(recipientName, appointmentDate, appointmentTime, doctorName);
            String textContent = buildAppointmentReminderTextContent(recipientName, appointmentDate, appointmentTime, doctorName);
            
            return simpleMailService.sendHtmlEmail(recipientEmail, recipientName, subject, htmlContent, textContent);
            
        } catch (Exception e) {
            logger.error("Failed to send appointment reminder email to {}: {}", recipientEmail, e.getMessage(), e);
            return false;
        }
    }
    
    private String buildWelcomeEmailContent(String recipientName, String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Welcome to Hospital Management System</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #2c3e50;">Welcome to Hospital Management System</h1>
                    <p>Dear %s,</p>
                    <p>Welcome to our Hospital Management System! Your account has been successfully created.</p>
                    <p><strong>Username:</strong> %s</p>
                    <p>You can now log in to access your account and manage your medical information.</p>
                    <p>If you have any questions, please don't hesitate to contact our support team.</p>
                    <p>Best regards,<br>Hospital Management Team</p>
                </div>
            </body>
            </html>
            """.formatted(recipientName, username);
    }
    
    private String buildWelcomeEmailTextContent(String recipientName, String username) {
        return String.format("""
            Welcome to Hospital Management System
            
            Dear %s,
            
            Welcome to our Hospital Management System! Your account has been successfully created.
            
            Username: %s
            
            You can now log in to access your account and manage your medical information.
            
            If you have any questions, please don't hesitate to contact our support team.
            
            Best regards,
            Hospital Management Team
            """, recipientName, username);
    }
    
    private String buildAppointmentConfirmationContent(String recipientName, String appointmentDate, 
                                                     String appointmentTime, String doctorName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Confirmation</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #27ae60;">Appointment Confirmed</h1>
                    <p>Dear %s,</p>
                    <p>Your appointment has been successfully confirmed with the following details:</p>
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Time:</strong> %s</p>
                        <p><strong>Doctor:</strong> %s</p>
                    </div>
                    <p>Please arrive 15 minutes before your scheduled appointment time.</p>
                    <p>If you need to reschedule or cancel, please contact us at least 24 hours in advance.</p>
                    <p>Best regards,<br>Hospital Management Team</p>
                </div>
            </body>
            </html>
            """.formatted(recipientName, appointmentDate, appointmentTime, doctorName);
    }
    
    private String buildAppointmentConfirmationTextContent(String recipientName, String appointmentDate, 
                                                         String appointmentTime, String doctorName) {
        return String.format("""
            Appointment Confirmed
            
            Dear %s,
            
            Your appointment has been successfully confirmed with the following details:
            
            Date: %s
            Time: %s
            Doctor: %s
            
            Please arrive 15 minutes before your scheduled appointment time.
            
            If you need to reschedule or cancel, please contact us at least 24 hours in advance.
            
            Best regards,
            Hospital Management Team
            """, recipientName, appointmentDate, appointmentTime, doctorName);
    }
    
    private String buildAppointmentReminderContent(String recipientName, String appointmentDate, 
                                                 String appointmentTime, String doctorName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Appointment Reminder</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #f39c12;">Appointment Reminder</h1>
                    <p>Dear %s,</p>
                    <p>This is a friendly reminder about your upcoming appointment:</p>
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #f39c12;">
                        <p><strong>Date:</strong> %s</p>
                        <p><strong>Time:</strong> %s</p>
                        <p><strong>Doctor:</strong> %s</p>
                    </div>
                    <p>Please arrive 15 minutes before your scheduled appointment time.</p>
                    <p>If you need to reschedule or cancel, please contact us as soon as possible.</p>
                    <p>Best regards,<br>Hospital Management Team</p>
                </div>
            </body>
            </html>
            """.formatted(recipientName, appointmentDate, appointmentTime, doctorName);
    }
    
    private String buildAppointmentReminderTextContent(String recipientName, String appointmentDate, 
                                                     String appointmentTime, String doctorName) {
        return String.format("""
            Appointment Reminder
            
            Dear %s,
            
            This is a friendly reminder about your upcoming appointment:
            
            Date: %s
            Time: %s
            Doctor: %s
            
            Please arrive 15 minutes before your scheduled appointment time.
            
            If you need to reschedule or cancel, please contact us as soon as possible.
            
            Best regards,
            Hospital Management Team
            """, recipientName, appointmentDate, appointmentTime, doctorName);
    }
}
            
           