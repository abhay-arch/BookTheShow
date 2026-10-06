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
@RequestMapping("/api/v1/screens")
@RequiredArgsConstructor
public class ScreenController {
    
    private final CatalogService catalogService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogDtos.ScreenResponse>>> getScreensByVenue(
            @RequestParam UUID venueId) {
        List<CatalogDtos.ScreenResponse> screens = catalogService.getScreensByVenue(venueId);
        return ResponseEntity.ok(ApiResponse.success(screens));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogDtos.ScreenResponse>> getScreenById(@PathVariable UUID id) {
        CatalogDtos.ScreenResponse screen = catalogService.getScreenById(id);
        return ResponseEntity.ok(ApiResponse.success(screen));
    }
    
    @GetMapping("/{id}/layout")
    public ResponseEntity<ApiResponse<CatalogDtos.ScreenLayoutResponse>> getScreenLayout(@PathVariable UUID id) {
        CatalogDtos.ScreenLayoutResponse layout = catalogService.getScreenLayout(id);
        return ResponseEntity.ok(ApiResponse.success(layout));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogDtos.ScreenResponse>> createScreen(
            @Valid @RequestBody CatalogDtos.CreateScreenRequest request) {
        CatalogDtos.ScreenResponse screen = catalogService.createScreen(request);
        return ResponseEntity.ok(ApiResponse.success(screen));
    }
}