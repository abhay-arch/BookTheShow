package com.bookmyshow.booking.repository;

import com.bookmyshow.booking.entity.BookingSeat;
import com.bookmyshow.booking.entity.BookingSeatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {
    
    List<BookingSeat> findByBookingId(UUID bookingId);
    
    List<BookingSeat> findByShowIdAndSeatId(UUID showId, UUID seatId);
    
    boolean existsByBookingIdAndSeatId(UUID bookingId, UUID seatId);
}