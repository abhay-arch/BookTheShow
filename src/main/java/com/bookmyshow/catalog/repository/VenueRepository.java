package com.bookmyshow.catalog.repository;

import com.bookmyshow.catalog.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {
    
    List<Venue> findByCityIdAndIsActiveTrue(UUID cityId);
    
    List<Venue> findByIsActiveTrue();
    
    @Query("SELECT v FROM Venue v WHERE v.cityId = :cityId AND v.isActive = true AND v.name ILIKE %:name%")
    List<Venue> searchByCityAndName(@Param("cityId") UUID cityId, @Param("name") String name);
}