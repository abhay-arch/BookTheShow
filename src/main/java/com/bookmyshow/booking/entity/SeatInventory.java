package com.bookmyshow.booking.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat_inventory", indexes = {
        @Index(name = "idx_seat_inventory_show", columnList = "show_id"),
        @Index(name = "idx_seat_inventory_seat", columnList = "seat_id"),
        @Index(name = "idx_seat_inventory_status", columnList = "status"),
        @Index(name = "idx_seat_inventory_hold", columnList = "hold_id"),
        @Index(name = "idx_seat_inventory_show_status", columnList = "show_id, status"),
        @Index(name = "idx_seat_inventory_unique", columnList = "show_id, seat_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInventory extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "show_id", nullable = false, columnDefinition = "UUID")
    private UUID showId;
    
    @Column(name = "seat_id", nullable = false, columnDefinition = "UUID")
    private UUID seatId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status;
    
    @Column(name = "hold_id", columnDefinition = "UUID")
    private UUID holdId;
    
    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;
    
    @Column(name = "booking_id", columnDefinition = "UUID")
    private UUID bookingId;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;
    
    public enum SeatStatus {
        AVAILABLE, HELD, BOOKED, BLOCKED
    }
}