package com.bookmyshow.catalog.entity;

import com.bookmyshow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movies_title", columnList = "title"),
        @Index(name = "idx_movies_language", columnList = "language"),
        @Index(name = "idx_movies_certification", columnList = "certification"),
        @Index(name = "idx_movies_release_date", columnList = "release_date"),
        @Index(name = "idx_movies_active", columnList = "is_active"),
        @Index(name = "idx_movies_coming_soon", columnList = "is_coming_soon")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "title", nullable = false, length = 300)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @Column(name = "language", nullable = false, length = 50)
    private String language;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "certification", nullable = false, length = 10)
    private Certification certification;
    
    @Column(name = "release_date", nullable = false)
    private Instant releaseDate;
    
    @Column(name = "end_date")
    private Instant endDate;
    
    @Column(name = "poster_url", length = 500)
    private String posterUrl;
    
    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;
    
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_coming_soon", nullable = false)
    @Builder.Default
    private Boolean isComingSoon = false;
    
    @Column(name = "director", length = 200)
    private String director;
    
    @Column(name = "cast_members", columnDefinition = "TEXT")
    private String castMembers;
    
    public enum Certification {
        U, UA, A, S
    }
}