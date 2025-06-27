package com.hms.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void processScheduledNotifications() {
        try {
            logger.info("Running scheduled notifications processing task...");
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            logger.error("Error in scheduled notifications processing: {}", e.getMessage(), e);
        }
    }
    
    @Scheduled(fixedRate = 900000) // Run every 15 minutes
    public void retryFailedNotifications() {
        try {
            logger.info("Running failed notifications retry task...");
            notificationService.retryFailedNotifications();
        } catch (Exception e) {
            logger.error("Error in failed notifications retry: {}", e.getMessage(), e);
        }
    }
}
