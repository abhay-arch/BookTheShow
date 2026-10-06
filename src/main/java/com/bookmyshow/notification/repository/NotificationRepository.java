package com.bookmyshow.notification.repository;

import com.bookmyshow.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<Notification> findByBookingId(UUID bookingId);
    
    List<Notification> findByStatusAndRetryCountLessThan(Notification.NotificationStatus status, int maxRetries);
    
    Page<Notification> findByUserId(UUID userId, Pageable pageable);
}