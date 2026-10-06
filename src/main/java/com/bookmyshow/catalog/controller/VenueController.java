package com.bookmyshow.catalog.controller;

import com.bookmyshow.catalog.dto.CatalogDtos;
import com.bookmyshow.catalog.service.CatalogService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {
    
    private final CatalogService catalogService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogDtos.VenueResponse>>> getVenuesByCity(
            @RequestParam UUID cityId) {
        List<CatalogDtos.VenueResponse> venues = catalogService.getVenuesByCity(cityId);
        return ResponseEntity.ok(ApiResponse.success(venues));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogDtos.VenueResponse>> getVenueById(@PathVariable UUID id) {
        CatalogDtos.VenueResponse venue = catalogService.getVenueById(id);
        return ResponseEntity.ok(ApiResponse.success(venue));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogDtos.VenueResponse>> createVenue(
            @Valid @RequestBody CatalogDtos.CreateVenueRequest request) {
        CatalogDtos.VenueResponse venue = catalogService.createVenue(request);
        return ResponseEntity.ok(ApiResponse.success(venue));
    }
}