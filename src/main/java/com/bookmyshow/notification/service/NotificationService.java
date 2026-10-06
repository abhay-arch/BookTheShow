package com.bookmyshow.notification.service;

import com.bookmyshow.notification.dto.NotificationDtos;
import com.bookmyshow.notification.entity.Notification;
import com.bookmyshow.notification.repository.NotificationRepository;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public NotificationDtos.NotificationResponse sendNotification(NotificationDtos.SendNotificationRequest request) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .bookingId(request.getBookingId())
                .type(request.getType())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .content(request.getContent())
                .status(Notification.NotificationStatus.PENDING)
                .retryCount(0)
                .build();
        
        notification = notificationRepository.save(notification);
        
        // In real implementation, send via email/SMS/push provider
        // For now, simulate success
        sendViaProvider(notification);
        
        notification.setStatus(Notification.NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notification = notificationRepository.save(notification);
        
        log.info("Sent notification {} to user {} via {}", notification.getId(), request.getUserId(), request.getChannel());
        
        return mapToResponse(notification);
    }
    
    public void retryFailedNotifications(int maxRetries) {
        List<Notification> failed = notificationRepository.findByStatusAndRetryCountLessThan(
                Notification.NotificationStatus.FAILED, maxRetries);
        
        for (Notification notification : failed) {
            try {
                sendViaProvider(notification);
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(Instant.now());
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setFailureReason(e.getMessage());
                notification.setFailedAt(Instant.now());
                
                if (notification.getRetryCount() >= maxRetries) {
                    notification.setStatus(Notification.NotificationStatus.FAILED);
                }
            }
            notificationRepository.save(notification);
        }
    }
    
    private void sendViaProvider(Notification notification) {
        // In real implementation, integrate with:
        // - Email: SendGrid, AWS SES, Mailgun
        // - SMS: Twilio, AWS SNS
        // - Push: Firebase, OneSignal
        // - WhatsApp: Twilio, Gupshup
        
        log.debug("Sending notification via {}: {}", notification.getChannel(), notification.getSubject());
        
        // Simulate provider call
        if (notification.getChannel() == Notification.Channel.EMAIL) {
            // sendEmail(notification);
        } else if (notification.getChannel() == Notification.Channel.SMS) {
            // sendSms(notification);
        } else if (notification.getChannel() == Notification.Channel.PUSH) {
            // sendPush(notification);
        }
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDtos.NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private NotificationDtos.NotificationResponse mapToResponse(Notification notification) {
        return NotificationDtos.NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .bookingId(notification.getBookingId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .retryCount(notification.getRetryCount())
                .build();
    }
}