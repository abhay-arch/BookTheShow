package com.bookmyshow.catalog.repository;

import com.bookmyshow.catalog.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, UUID> {
    
    List<Screen> findByVenueIdAndIsActiveTrue(UUID venueId);
    
    List<Screen> findByIsActiveTrue();
    
    long countByVenueIdAndIsActiveTrue(UUID venueId);
}