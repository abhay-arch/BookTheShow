package com.bookmyshow.catalog.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "screens", indexes = {
        @Index(name = "idx_screens_venue", columnList = "venue_id"),
        @Index(name = "idx_screens_name", columnList = "name"),
        @Index(name = "idx_screens_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "venue_id", nullable = false, columnDefinition = "UUID")
    private UUID venueId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "screen_type", nullable = false, length = 30)
    private ScreenType screenType;
    
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;
    
    @Column(name = "total_columns", nullable = false)
    private Integer totalColumns;
    
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    public enum ScreenType {
        STANDARD, IMAX, FOUR_DX, DOLBY_ATMOS, SCREEN_X, PREMIUM, GOLD_CLASS
    }
}