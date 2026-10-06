package com.bookmyshow.catalog.repository;

import com.bookmyshow.catalog.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {
    
    Optional<Movie> findByTitleIgnoreCase(String title);
    
    List<Movie> findByLanguageIgnoreCaseAndIsActiveTrue(String language);
    
    List<Movie> findByIsActiveTrueAndIsComingSoonFalse();
    
    List<Movie> findByIsComingSoonTrueAndIsActiveTrue();
    
    Page<Movie> findByIsComingSoonTrueAndIsActiveTrue(Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE m.isActive = true AND m.releaseDate <= :now AND (m.endDate IS NULL OR m.endDate >= :now)")
    List<Movie> findCurrentlyPlaying(@Param("now") Instant now);
    
    @Query("SELECT m FROM Movie m WHERE m.isActive = true AND m.releaseDate > :now")
    List<Movie> findUpcomingMovies(@Param("now") Instant now);
    
    Page<Movie> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT m FROM Movie m " +
           "WHERE m.isActive = true " +
           "AND (:cityId IS NULL OR EXISTS (SELECT 1 FROM Show s JOIN Screen sc ON s.screenId = sc.id " +
           "WHERE s.movieId = m.id AND sc.venueId IN (SELECT v.id FROM Venue v WHERE v.cityId = :cityId) " +
           "AND s.status IN ('ON_SALE', 'SOLD_OUT') AND s.startTime >= :now)) " +
           "AND (:query IS NULL OR m.title ILIKE %:query% OR m.description ILIKE %:query%) " +
           "AND (:language IS NULL OR m.language ILIKE :language) " +
           "ORDER BY m.releaseDate DESC")
    Page<Movie> searchMovies(
            @Param("cityId") UUID cityId,
            @Param("query") String query,
            @Param("language") String language,
            @Param("now") Instant now,
            Pageable pageable);
}