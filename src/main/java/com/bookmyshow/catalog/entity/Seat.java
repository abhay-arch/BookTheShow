package com.bookmyshow.catalog.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seats", indexes = {
        @Index(name = "idx_seats_screen", columnList = "screen_id"),
        @Index(name = "idx_seats_row_number", columnList = "screen_id, row_label, seat_number", unique = true),
        @Index(name = "idx_seats_type", columnList = "seat_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "screen_id", nullable = false, columnDefinition = "UUID")
    private UUID screenId;
    
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;
    
    @Column(name = "is_accessible", nullable = false)
    @Builder.Default
    private Boolean isAccessible = false;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    public enum SeatType {
        STANDARD, PREMIUM, RECLINER, VIP, COUPLE, WHEELCHAIR
    }
    
    public String getDisplayLabel() {
        return rowLabel + seatNumber;
    }
}