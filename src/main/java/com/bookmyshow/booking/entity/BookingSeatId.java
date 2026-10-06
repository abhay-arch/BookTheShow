package com.bookmyshow.booking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeatId implements Serializable {
    
    @Column(name = "booking_id", nullable = false, columnDefinition = "UUID")
    private UUID bookingId;
    
    @Column(name = "seat_id", nullable = false, columnDefinition = "UUID")
    private UUID seatId;
}