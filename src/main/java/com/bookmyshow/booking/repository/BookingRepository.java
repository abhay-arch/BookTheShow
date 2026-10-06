package com.bookmyshow.booking.repository;

import com.bookmyshow.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
    
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Page<Booking> findByUserId(UUID userId, Pageable pageable);
    
    List<Booking> findByShowIdAndStatusIn(UUID showId, List<Booking.BookingStatus> statuses);
    
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING_PAYMENT' AND b.expiresAt < :now")
    List<Booking> findExpiredPendingBookings(@Param("now") Instant now);
    
    @Query("SELECT b FROM Booking b WHERE b.showId = :showId AND b.status IN ('CONFIRMED', 'PENDING_PAYMENT')")
    List<Booking> findActiveBookingsForShow(@Param("showId") UUID showId);
}