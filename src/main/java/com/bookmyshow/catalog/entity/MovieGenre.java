package com.bookmyshow.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "movie_genres", indexes = {
        @Index(name = "idx_movie_genres_movie", columnList = "movie_id"),
        @Index(name = "idx_movie_genres_genre", columnList = "genre_id"),
        @Index(name = "idx_movie_genres_unique", columnList = "movie_id, genre_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieGenre {
    
    @Id
    @Column(name = "movie_id", nullable = false, columnDefinition = "UUID")
    private UUID movieId;
    
    @Id
    @Column(name = "genre_id", nullable = false, columnDefinition = "UUID")
    private UUID genreId;
}