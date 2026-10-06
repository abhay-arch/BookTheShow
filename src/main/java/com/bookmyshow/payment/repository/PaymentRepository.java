package com.bookmyshow.payment.repository;

import com.bookmyshow.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByBookingId(UUID bookingId);
    
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    
    Optional<Payment> findByProviderAndProviderPaymentId(String provider, String providerPaymentId);
    
    List<Payment> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);
    
    Page<Payment> findByStatusIn(List<Payment.PaymentStatus> statuses, Pageable pageable);
    
    List<Payment> findByStatusAndCreatedAtBefore(Payment.PaymentStatus status, java.time.Instant before);
}