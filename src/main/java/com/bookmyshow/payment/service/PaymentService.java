package com.bookmyshow.payment.service;

import com.bookmyshow.payment.dto.PaymentDtos;
import com.bookmyshow.payment.entity.Payment;
import com.bookmyshow.payment.repository.PaymentRepository;
import com.bookmyshow.booking.dto.BookingDtos;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.repository.BookingRepository;
import com.bookmyshow.booking.service.BookingService;
import com.bookmyshow.common.entity.OutboxEvent;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import com.bookmyshow.common.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final OutboxEventRepository outboxEventRepository;
    
    @Value("${bookmyshow.payment.provider.razorpay.key-id:}")
    private String razorpayKeyId;
    
    @Value("${bookmyshow.payment.provider.razorpay.key-secret:}")
    private String razorpayKeySecret;
    
    @Value("${bookmyshow.payment.webhook.signature-header:X-Signature}")
    private String webhookSignatureHeader;
    
    @Transactional
    public PaymentDtos.PaymentResponse initiatePayment(UUID userId, PaymentDtos.InitiatePaymentRequest request) {
        // Check idempotency
        if (request.getIdempotencyKey() != null) {
            paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
                    });
        }
        
        // Validate booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "Booking is not in pending payment state");
        }
        
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.HOLD_EXPIRED);
        }
        
        // Check if payment already exists for this booking
        paymentRepository.findByBookingId(booking.getId()).ifPresent(existing -> {
            if (existing.getStatus() == Payment.PaymentStatus.SUCCESS ||
                existing.getStatus() == Payment.PaymentStatus.PENDING) {
                throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED);
            }
        });
        
        // Create payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bookingId(booking.getId())
                .provider(request.getProvider())
                .status(Payment.PaymentStatus.INITIATED)
                .amount(booking.getTotalAmount())
                .currency("INR")
                .idempotencyKey(request.getIdempotencyKey())
                .paymentMethod(request.getPaymentMethod())
                .build();
        
        payment = paymentRepository.save(payment);
        
        // In real implementation, call payment provider API here
        // For now, simulate provider response
        String providerOrderId = "order_" + UUID.randomUUID().toString().substring(0, 12);
        String paymentUrl = "https://payment-provider.com/pay/" + providerOrderId;
        
        payment.setProviderOrderId(providerOrderId);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);
        
        log.info("Initiated payment {} for booking {}", payment.getId(), booking.getId());
        
        return PaymentDtos.PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .provider(payment.getProvider())
                .providerOrderId(providerOrderId)
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentUrl(paymentUrl)
                .expiresAt(booking.getExpiresAt())
                .idempotencyKey(payment.getIdempotencyKey())
                .build();
    }
    
    @Transactional
    public PaymentDtos.PaymentDetailsResponse getPaymentDetails(UUID paymentId, UUID userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        return mapToDetailsResponse(payment);
    }
    
    @Transactional
    public PaymentDtos.RefundResponse refundPayment(UUID userId, PaymentDtos.RefundRequest request) {
        // Check idempotency
        if (request.getIdempotencyKey() != null) {
            paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
                    });
        }
        
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED, "Payment not successful");
        }
        
        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : payment.getAmount();
        
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Refund amount cannot exceed payment amount");
        }
        
        // Call payment provider refund API
        // For now, simulate success
        String refundId = "refund_" + UUID.randomUUID().toString().substring(0, 12);
        
        payment.setStatus(Payment.PaymentStatus.REFUND_PENDING);
        payment.setRefundAmount(refundAmount);
        paymentRepository.save(payment);
        
        // Simulate refund completion
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now());
        paymentRepository.save(payment);
        
        // Update booking status
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason("Refunded: " + request.getReason());
        bookingRepository.save(booking);
        
        // Publish refund event
        publishPaymentRefundedEvent(payment);
        
        log.info("Refunded payment {} for booking {}", payment.getId(), booking.getId());
        
        return PaymentDtos.RefundResponse.builder()
                .paymentId(payment.getId())
                .refundAmount(refundAmount)
                .status(payment.getStatus())
                .refundedAt(payment.getRefundedAt())
                .build();
    }
    
    @Transactional
    public void handleWebhook(PaymentDtos.WebhookPayload payload, String signature) {
        // Verify webhook signature
        if (!verifyWebhookSignature(payload, signature)) {
            throw new BusinessException(ErrorCode.PAYMENT_PROVIDER_ERROR, "Invalid webhook signature");
        }
        
        // Find payment by provider ID
        Payment payment = paymentRepository
                .findByProviderAndProviderPaymentId(payload.getProvider(), payload.getProviderPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        
        // Check if already processed (idempotency)
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS ||
            payment.getStatus() == Payment.PaymentStatus.FAILED) {
            log.info("Webhook for already processed payment: {}", payment.getId());
            return;
        }
        
        // Update payment based on webhook
        payment.setProviderPaymentId(payload.getProviderPaymentId());
        payment.setWebhookPayload(payload.getPayload());
        payment.setStatus(payload.getStatus());
        
        if (payload.getStatus() == Payment.PaymentStatus.SUCCESS) {
            payment.setPaidAt(Instant.now());
            
            // Confirm booking
            bookingService.confirmBooking(payment.getBookingId(), 
                    bookingRepository.findById(payment.getBookingId()).get().getUserId());
            
            // Publish payment success event
            publishPaymentSuccessEvent(payment);
        } else if (payload.getStatus() == Payment.PaymentStatus.FAILED) {
            payment.setFailedAt(Instant.now());
            payment.setFailureReason(payload.getPayload());
            
            // Release seats
            bookingService.cancelBooking(payment.getBookingId(),
                    bookingRepository.findById(payment.getBookingId()).get().getUserId(),
                    new BookingDtos.CancelBookingRequest());
            
            // Publish payment failed event
            publishPaymentFailedEvent(payment);
        }
        
        paymentRepository.save(payment);
        log.info("Processed webhook for payment: {}", payment.getId());
    }
    
    private boolean verifyWebhookSignature(PaymentDtos.WebhookPayload payload, String signature) {
        // In real implementation, verify HMAC signature with provider secret
        // For now, return true for testing
        return true;
    }
    
    private PaymentDtos.PaymentDetailsResponse mapToDetailsResponse(Payment payment) {
        return PaymentDtos.PaymentDetailsResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .provider(payment.getProvider())
                .providerPaymentId(payment.getProviderPaymentId())
                .providerOrderId(payment.getProviderOrderId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .failedAt(payment.getFailedAt())
                .failureReason(payment.getFailureReason())
                .refundAmount(payment.getRefundAmount())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
    
    private void publishPaymentSuccessEvent(Payment payment) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(payment.getId())
                .eventType("PaymentSucceeded")
                .payload(String.format("{\"paymentId\":\"%s\",\"bookingId\":\"%s\",\"amount\":%s}",
                        payment.getId(), payment.getBookingId(), payment.getAmount()))
                .build();
        outboxEventRepository.save(event);
    }
    
    private void publishPaymentFailedEvent(Payment payment) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(payment.getId())
                .eventType("PaymentFailed")
                .payload(String.format("{\"paymentId\":\"%s\",\"bookingId\":\"%s\",\"reason\":\"%s\"}",
                        payment.getId(), payment.getBookingId(), payment.getFailureReason()))
                .build();
        outboxEventRepository.save(event);
    }
    
    private void publishPaymentRefundedEvent(Payment payment) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(payment.getId())
                .eventType("PaymentRefunded")
                .payload(String.format("{\"paymentId\":\"%s\",\"bookingId\":\"%s\",\"refundAmount\":%s}",
                        payment.getId(), payment.getBookingId(), payment.getRefundAmount()))
                .build();
        outboxEventRepository.save(event);
    }
}