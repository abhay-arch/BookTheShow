package com.bookmyshow.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_seats", indexes = {
        @Index(name = "idx_booking_seats_booking", columnList = "booking_id"),
        @Index(name = "idx_booking_seats_show", columnList = "show_id"),
        @Index(name = "idx_booking_seats_seat", columnList = "seat_id"),
        @Index(name = "idx_booking_seats_unique", columnList = "booking_id, seat_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BookingSeatId.class)
public class BookingSeat implements Serializable {
    
    @Id
    @Column(name = "booking_id", nullable = false, columnDefinition = "UUID")
    private UUID bookingId;
    
    @Id
    @Column(name = "seat_id", nullable = false, columnDefinition = "UUID")
    private UUID seatId;
    
    @Column(name = "show_id", nullable = false, columnDefinition = "UUID")
    private UUID showId;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}