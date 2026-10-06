package com.bookmyshow.show.controller;

import com.bookmyshow.show.dto.ShowDtos;
import com.bookmyshow.show.service.ShowService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {
    
    private final ShowService showService;
    
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<ShowDtos.ShowListResponse>>> getShowsForMovie(
            @PathVariable UUID movieId,
            @RequestParam(required = false) UUID cityId) {
        List<ShowDtos.ShowListResponse> shows = showService.getShowsForMovie(movieId, cityId);
        return ResponseEntity.ok(ApiResponse.success(shows));
    }
    
    @GetMapping("/city/{cityId}")
    public ResponseEntity<ApiResponse<List<ShowDtos.ShowListResponse>>> getShowsInCity(
            @PathVariable UUID cityId,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        List<ShowDtos.ShowListResponse> shows = showService.getShowsInCity(cityId, from, to);
        return ResponseEntity.ok(ApiResponse.success(shows));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowDtos.ShowResponse>> getShowById(@PathVariable UUID id) {
        ShowDtos.ShowResponse show = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(show));
    }
    
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<ShowDtos.ShowAvailabilityResponse>> getShowAvailability(@PathVariable UUID id) {
        ShowDtos.ShowAvailabilityResponse availability = showService.getShowAvailability(id);
        return ResponseEntity.ok(ApiResponse.success(availability));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShowDtos.ShowResponse>> createShow(
            @Valid @RequestBody ShowDtos.CreateShowRequest request) {
        ShowDtos.ShowResponse show = showService.createShow(request);
        return ResponseEntity.ok(ApiResponse.success(show));
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShowDtos.ShowResponse>> updateShow(
            @PathVariable UUID id,
            @Valid @RequestBody ShowDtos.UpdateShowRequest request) {
        ShowDtos.ShowResponse show = showService.updateShow(id, request);
        return ResponseEntity.ok(ApiResponse.success(show));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteShow(@PathVariable UUID id) {
        showService.deleteShow(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}