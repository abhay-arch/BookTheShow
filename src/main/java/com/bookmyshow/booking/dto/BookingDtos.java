package com.bookmyshow.booking.dto;

import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.catalog.entity.Seat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class BookingDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBookingRequest {
        @NotNull private UUID showId;
        @NotNull private List<UUID> seatIds;
        @Size(max = 100) private String idempotencyKey;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldSeatsRequest {
        @NotNull private UUID showId;
        @NotNull private List<UUID> seatIds;
        @Size(max = 100) private String idempotencyKey;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingResponse {
        private UUID id;
        private UUID userId;
        private UUID showId;
        private String showTitle;
        private Instant showTime;
        private String venueName;
        private String screenName;
        private Booking.BookingStatus status;
        private BigDecimal totalAmount;
        private Integer seatCount;
        private List<BookedSeatResponse> seats;
        private Instant expiresAt;
        private Instant confirmedAt;
        private String idempotencyKey;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookedSeatResponse {
        private UUID seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String displayLabel;
        private Seat.SeatType seatType;
        private BigDecimal price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoldResponse {
        private UUID holdId;
        private UUID showId;
        private List<HeldSeatResponse> seats;
        private Instant expiresAt;
        private Integer holdDurationMinutes;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeldSeatResponse {
        private UUID seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String displayLabel;
        private Seat.SeatType seatType;
        private BigDecimal price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingListResponse {
        private UUID id;
        private UUID showId;
        private String movieTitle;
        private Instant showTime;
        private String venueName;
        private Booking.BookingStatus status;
        private BigDecimal totalAmount;
        private Integer seatCount;
        private Instant createdAt;
        private Instant confirmedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelBookingRequest {
        @Size(max = 500) private String reason;
    }
}