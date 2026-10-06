package com.bookmyshow.catalog.repository;

import com.bookmyshow.catalog.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {
    
    Optional<Genre> findByNameIgnoreCase(String name);
    
    List<Genre> findAllByOrderByNameAsc();
}