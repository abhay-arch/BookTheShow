package com.bookmyshow.booking.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_user", columnList = "user_id"),
        @Index(name = "idx_bookings_show", columnList = "show_id"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_bookings_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;
    
    @Column(name = "show_id", nullable = false, columnDefinition = "UUID")
    private UUID showId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal totalAmount;
    
    @Column(name = "seat_count", nullable = false)
    private Integer seatCount;
    
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;
    
    @Column(name = "expires_at")
    private Instant expiresAt;
    
    @Column(name = "confirmed_at")
    private Instant confirmedAt;
    
    @Column(name = "cancelled_at")
    private Instant cancelledAt;
    
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;
    
    public enum BookingStatus {
        PENDING_PAYMENT, CONFIRMED, CANCELLED, EXPIRED, FAILED
    }
}