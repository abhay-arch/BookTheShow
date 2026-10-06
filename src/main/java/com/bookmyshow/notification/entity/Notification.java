package com.bookmyshow.notification.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user", columnList = "user_id"),
        @Index(name = "idx_notifications_type", columnList = "type"),
        @Index(name = "idx_notifications_status", columnList = "status"),
        @Index(name = "idx_notifications_booking", columnList = "booking_id"),
        @Index(name = "idx_notifications_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;
    
    @Column(name = "booking_id", columnDefinition = "UUID")
    private UUID bookingId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private Channel channel;
    
    @Column(name = "subject", length = 200)
    private String subject;
    
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;
    
    @Column(name = "sent_at")
    private Instant sentAt;
    
    @Column(name = "failed_at")
    private Instant failedAt;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "external_id", length = 100)
    private String externalId;
    
    public enum NotificationType {
        BOOKING_CONFIRMATION, BOOKING_CANCELLATION, PAYMENT_SUCCESS, PAYMENT_FAILED,
        REFUND_INITIATED, REFUND_COMPLETED, SHOW_REMINDER, SEAT_HOLD_EXPIRY,
        PROMOTIONAL, ACCOUNT_VERIFICATION, PASSWORD_RESET
    }
    
    public enum Channel {
        EMAIL, SMS, PUSH, WHATSAPP
    }
    
    public enum NotificationStatus {
        PENDING, SENT, FAILED, CANCELLED
    }
}