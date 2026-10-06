package com.bookmyshow.catalog.repository;

import com.bookmyshow.catalog.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {
    
    List<Seat> findByScreenIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(UUID screenId);
    
    Optional<Seat> findByScreenIdAndRowLabelAndSeatNumber(UUID screenId, String rowLabel, Integer seatNumber);
    
    List<Seat> findByScreenIdAndSeatTypeAndIsActiveTrue(UUID screenId, Seat.SeatType seatType);
    
    long countByScreenId(UUID screenId);
}