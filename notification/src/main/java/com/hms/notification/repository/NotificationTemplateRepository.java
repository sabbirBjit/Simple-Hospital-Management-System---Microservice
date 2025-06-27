package com.hms.notification.repository;

import com.hms.notification.model.NotificationTemplate;
import com.hms.notification.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByNameAndIsActive(String name, Boolean isActive);
    
    List<NotificationTemplate> findByTypeAndIsActiveOrderByName(NotificationType type, Boolean isActive);
    
    List<NotificationTemplate> findByCategoryAndIsActiveOrderByName(String category, Boolean isActive);
    
    Optional<NotificationTemplate> findByName(String name);
}
