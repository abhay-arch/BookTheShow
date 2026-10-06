package com.bookmyshow.show.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shows", indexes = {
        @Index(name = "idx_shows_movie", columnList = "movie_id"),
        @Index(name = "idx_shows_screen", columnList = "screen_id"),
        @Index(name = "idx_shows_start_time", columnList = "start_time"),
        @Index(name = "idx_shows_status", columnList = "status"),
        @Index(name = "idx_shows_movie_screen_time", columnList = "movie_id, screen_id, start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Show extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "movie_id", nullable = false, columnDefinition = "UUID")
    private UUID movieId;
    
    @Column(name = "screen_id", nullable = false, columnDefinition = "UUID")
    private UUID screenId;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowStatus status;
    
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal basePrice;
    
    @Column(name = "language", length = 50)
    private String language;
    
    @Column(name = "is_3d", nullable = false)
    @Builder.Default
    private Boolean is3D = false;
    
    @Column(name = "is_atmos", nullable = false)
    @Builder.Default
    private Boolean isAtmos = false;
    
    public enum ShowStatus {
        SCHEDULED, ON_SALE, SOLD_OUT, CANCELLED, COMPLETED
    }
}