package com.bookmyshow.booking.repository;

import com.bookmyshow.booking.entity.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, UUID> {
    
    List<SeatInventory> findByShowId(UUID showId);
    
    List<SeatInventory> findByShowIdAndStatus(UUID showId, SeatInventory.SeatStatus status);
    
    @Query("SELECT si FROM SeatInventory si WHERE si.showId = :showId AND si.seatId IN :seatIds")
    List<SeatInventory> findByShowIdAndSeatIds(@Param("showId") UUID showId, @Param("seatIds") List<UUID> seatIds);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM SeatInventory si WHERE si.showId = :showId AND si.seatId IN :seatIds")
    List<SeatInventory> findByShowIdAndSeatIdsForUpdate(@Param("showId") UUID showId, @Param("seatIds") List<UUID> seatIds);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM SeatInventory si WHERE si.showId = :showId AND si.status = 'AVAILABLE'")
    List<SeatInventory> findAvailableSeatsForUpdate(@Param("showId") UUID showId);
    
    @Query("SELECT COUNT(si) FROM SeatInventory si WHERE si.showId = :showId AND si.status = 'AVAILABLE'")
    long countAvailableSeats(@Param("showId") UUID showId);
    
    @Query("SELECT COUNT(si) FROM SeatInventory si WHERE si.showId = :showId AND si.status = 'HELD' AND si.holdExpiresAt < :now")
    long countExpiredHolds(@Param("showId") UUID showId, @Param("now") Instant now);
    
    @Query("SELECT si FROM SeatInventory si WHERE si.showId = :showId AND si.status = 'HELD' AND si.holdExpiresAt < :now")
    List<SeatInventory> findExpiredHolds(@Param("showId") UUID showId, @Param("now") Instant now);
    
    @Modifying
    @Query("UPDATE SeatInventory si SET si.status = 'AVAILABLE', si.holdId = NULL, si.holdExpiresAt = NULL " +
           "WHERE si.showId = :showId AND si.status = 'HELD' AND si.holdExpiresAt < :now")
    int releaseExpiredHolds(@Param("showId") UUID showId, @Param("now") Instant now);
    
    Optional<SeatInventory> findByShowIdAndSeatId(@Param("showId") UUID showId, @Param("seatId") UUID seatId);
}