package com.bookmyshow.payment.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_booking", columnList = "booking_id"),
        @Index(name = "idx_payments_provider", columnList = "provider"),
        @Index(name = "idx_payments_provider_id", columnList = "provider_payment_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_payments_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "booking_id", nullable = false, columnDefinition = "UUID")
    private UUID bookingId;
    
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;
    
    @Column(name = "provider_payment_id", length = 100)
    private String providerPaymentId;
    
    @Column(name = "provider_order_id", length = 100)
    private String providerOrderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "paid_at")
    private Instant paidAt;
    
    @Column(name = "failed_at")
    private Instant failedAt;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "refunded_at")
    private Instant refundedAt;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private java.math.BigDecimal refundAmount;
    
    @Column(name = "webhook_payload", columnDefinition = "TEXT")
    private String webhookPayload;
    
    public enum PaymentStatus {
        INITIATED, PENDING, SUCCESS, FAILED, REFUND_PENDING, REFUNDED, CANCELLED
    }
}