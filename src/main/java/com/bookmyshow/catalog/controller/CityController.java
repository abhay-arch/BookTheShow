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
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {
    
    private final CatalogService catalogService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CatalogDtos.CityResponse>>> getAllCities() {
        List<CatalogDtos.CityResponse> cities = catalogService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CatalogDtos.CityResponse>> getCityById(@PathVariable UUID id) {
        CatalogDtos.CityResponse city = catalogService.getCityById(id);
        return ResponseEntity.ok(ApiResponse.success(city));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogDtos.CityResponse>> createCity(
            @Valid @RequestBody CatalogDtos.CreateCityRequest request) {
        CatalogDtos.CityResponse city = catalogService.createCity(request);
        return ResponseEntity.ok(ApiResponse.success(city));
    }
}