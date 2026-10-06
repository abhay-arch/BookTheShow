package com.bookmyshow.payment.dto;

import com.bookmyshow.payment.entity.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatePaymentRequest {
        @NotNull private UUID bookingId;
        @NotNull private String provider;
        @Size(max = 100) private String idempotencyKey;
        @Size(max = 50) private String paymentMethod;
        private String returnUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private UUID id;
        private UUID bookingId;
        private String provider;
        private String providerPaymentId;
        private String providerOrderId;
        private Payment.PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String paymentUrl;
        private Instant expiresAt;
        private String idempotencyKey;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDetailsResponse {
        private UUID id;
        private UUID bookingId;
        private String provider;
        private String providerPaymentId;
        private String providerOrderId;
        private Payment.PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private Instant paidAt;
        private Instant failedAt;
        private String failureReason;
        private BigDecimal refundAmount;
        private Instant refundedAt;
        private Instant createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequest {
        @NotNull private UUID paymentId;
        @Size(max = 100) private String idempotencyKey;
        private BigDecimal amount;
        @Size(max = 500) private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundResponse {
        private UUID paymentId;
        private BigDecimal refundAmount;
        private Payment.PaymentStatus status;
        private Instant refundedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebhookPayload {
        private String event;
        private String provider;
        private String providerPaymentId;
        private String providerOrderId;
        private Payment.PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private String payload;
    }
}