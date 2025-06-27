package com.hms.notification.repository;

import com.hms.notification.model.Notification;
import com.hms.notification.model.NotificationStatus;
import com.hms.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByStatusAndRetryCountLessThanAndScheduledAtBefore(
        NotificationStatus status, Integer maxRetries, LocalDateTime scheduledAt);
    
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
        Long userId, NotificationType type, Pageable pageable);
    
    List<Notification> findByAppointmentIdOrderByCreatedAtDesc(Long appointmentId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findNotificationsByStatusAndDateRange(
        @Param("status") NotificationStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    Long countByStatusAndCreatedAtAfter(NotificationStatus status, LocalDateTime date);
}
