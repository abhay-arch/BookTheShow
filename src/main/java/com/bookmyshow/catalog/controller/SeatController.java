package com.bookmyshow.catalog.controller;

import com.bookmyshow.catalog.dto.CatalogDtos;
import com.bookmyshow.catalog.service.CatalogService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seats")
@RequiredArgsConstructor
public class SeatController {
    
    private final CatalogService catalogService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CatalogDtos.SeatResponse>> createSeat(
            @Valid @RequestBody CatalogDtos.CreateSeatRequest request) {
        CatalogDtos.SeatResponse seat = catalogService.createSeat(request);
        return ResponseEntity.ok(ApiResponse.success(seat));
    }
}