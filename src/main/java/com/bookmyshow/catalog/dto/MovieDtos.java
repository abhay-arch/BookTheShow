package com.bookmyshow.catalog.dto;

import com.bookmyshow.catalog.entity.Movie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MovieDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieResponse {
        private UUID id;
        private String title;
        private String description;
        private Integer durationMinutes;
        private String language;
        private Movie.Certification certification;
        private Instant releaseDate;
        private Instant endDate;
        private String posterUrl;
        private String trailerUrl;
        private BigDecimal rating;
        private Boolean isActive;
        private Boolean isComingSoon;
        private String director;
        private String cast;
        private List<GenreResponse> genres;
        
        public static MovieResponse from(Movie movie, List<GenreResponse> genres) {
            return MovieResponse.builder()
                    .id(movie.getId())
                    .title(movie.getTitle())
                    .description(movie.getDescription())
                    .durationMinutes(movie.getDurationMinutes())
                    .language(movie.getLanguage())
                    .certification(movie.getCertification())
                    .releaseDate(movie.getReleaseDate())
                    .endDate(movie.getEndDate())
                    .posterUrl(movie.getPosterUrl())
                    .trailerUrl(movie.getTrailerUrl())
                    .rating(movie.getRating())
                    .isActive(movie.getIsActive())
                    .isComingSoon(movie.getIsComingSoon())
                    .director(movie.getDirector())
                    .cast(movie.getCastMembers())
                    .genres(genres)
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieListResponse {
        private UUID id;
        private String title;
        private Integer durationMinutes;
        private String language;
        private Movie.Certification certification;
        private Instant releaseDate;
        private String posterUrl;
        private BigDecimal rating;
        private Boolean isComingSoon;
        private List<String> genreNames;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreResponse {
        private UUID id;
        private String name;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateMovieRequest {
        @NotBlank @Size(max = 300) private String title;
        @Size(max = 2000) private String description;
        @NotNull private Integer durationMinutes;
        @NotBlank @Size(max = 50) private String language;
        @NotNull private Movie.Certification certification;
        @NotNull private Instant releaseDate;
        private Instant endDate;
        @Size(max = 500) private String posterUrl;
        @Size(max = 500) private String trailerUrl;
        @Size(max = 200) private String director;
        @Size(max = 1000) private String castMembers;
        private List<UUID> genreIds;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMovieRequest {
        @Size(max = 300) private String title;
        @Size(max = 2000) private String description;
        private Integer durationMinutes;
        @Size(max = 50) private String language;
        private Movie.Certification certification;
        private Instant releaseDate;
        private Instant endDate;
        @Size(max = 500) private String posterUrl;
        @Size(max = 500) private String trailerUrl;
        private BigDecimal rating;
        private Boolean isActive;
        private Boolean isComingSoon;
        @Size(max = 200) private String director;
        @Size(max = 1000) private String castMembers;
        private List<UUID> genreIds;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGenreRequest {
        @NotBlank @Size(max = 50) private String name;
        @Size(max = 200) private String description;
    }
}