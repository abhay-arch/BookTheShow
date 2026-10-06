package com.bookmyshow.notification.dto;

import com.bookmyshow.notification.entity.Notification;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

public class NotificationDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private UUID id;
        private UUID userId;
        private UUID bookingId;
        private Notification.NotificationType type;
        private Notification.Channel channel;
        private String subject;
        private String content;
        private Notification.NotificationStatus status;
        private Instant sentAt;
        private Integer retryCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendNotificationRequest {
        private UUID userId;
        private UUID bookingId;
        private Notification.NotificationType type;
        private Notification.Channel channel;
        private String subject;
        private String content;
    }
}