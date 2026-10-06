package com.bookmyshow.show.repository;

import com.bookmyshow.show.entity.Show;
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
public interface ShowRepository extends JpaRepository<Show, UUID> {
    
    List<Show> findByMovieIdAndStatusIn(UUID movieId, List<Show.ShowStatus> statuses);
    
    List<Show> findByScreenIdAndStartTimeBetween(UUID screenId, Instant from, Instant to);
    
    @Query("SELECT s FROM Show s " +
           "WHERE s.movieId = :movieId " +
           "AND s.status IN ('ON_SALE', 'SOLD_OUT') " +
           "AND s.startTime >= :now " +
           "AND (:cityId IS NULL OR s.screenId IN (SELECT sc.id FROM Screen sc WHERE sc.venueId IN (SELECT v.id FROM Venue v WHERE v.cityId = :cityId))) " +
           "ORDER BY s.startTime ASC")
    List<Show> findShowsForMovie(
            @Param("movieId") UUID movieId,
            @Param("cityId") UUID cityId,
            @Param("now") Instant now);
    
    @Query("SELECT s FROM Show s " +
           "WHERE s.screenId IN (SELECT sc.id FROM Screen sc WHERE sc.venueId IN (SELECT v.id FROM Venue v WHERE v.cityId = :cityId)) " +
           "AND s.status IN ('ON_SALE', 'SOLD_OUT') " +
           "AND s.startTime >= :from " +
           "AND s.startTime <= :to " +
           "ORDER BY s.startTime ASC")
    List<Show> findShowsInCity(
            @Param("cityId") UUID cityId,
            @Param("from") Instant from,
            @Param("to") Instant to);
    
    Page<Show> findByStatusIn(List<Show.ShowStatus> statuses, Pageable pageable);
    
    @Query("SELECT s FROM Show s WHERE s.id = :id AND s.status IN ('ON_SALE', 'SOLD_OUT')")
    Optional<Show> findBookableShowById(@Param("id") UUID id);
}