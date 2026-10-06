package com.bookmyshow.booking.controller;

import com.bookmyshow.booking.dto.BookingDtos;
import com.bookmyshow.booking.service.BookingService;
import com.bookmyshow.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.bookmyshow.user.entity.User;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<BookingDtos.HoldResponse>> holdSeats(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BookingDtos.HoldSeatsRequest request) {
        
        BookingDtos.HoldResponse response = bookingService.holdSeats(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<ApiResponse<BookingDtos.BookingResponse>> confirmBooking(
            @AuthenticationPrincipal User user,
            @PathVariable UUID bookingId) {
        
        BookingDtos.BookingResponse response = bookingService.confirmBooking(bookingId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDtos.BookingResponse>> getBooking(
            @AuthenticationPrincipal User user,
            @PathVariable UUID bookingId) {
        
        BookingDtos.BookingResponse response = bookingService.getBookingById(bookingId, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDtos.BookingListResponse>>> getUserBookings(
            @AuthenticationPrincipal User user) {
        
        List<BookingDtos.BookingListResponse> bookings = bookingService.getUserBookings(user.getId());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
    
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingDtos.BookingResponse>> cancelBooking(
            @AuthenticationPrincipal User user,
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingDtos.CancelBookingRequest request) {
        
        BookingDtos.BookingResponse response = bookingService.cancelBooking(bookingId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}