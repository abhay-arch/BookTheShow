package com.bookmyshow.show.dto;

import com.bookmyshow.show.entity.Show;
import com.bookmyshow.catalog.entity.Screen;
import com.bookmyshow.catalog.entity.Seat;
import com.bookmyshow.booking.entity.SeatInventory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ShowDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowResponse {
        private UUID id;
        private UUID movieId;
        private String movieTitle;
        private UUID screenId;
        private String screenName;
        private String venueName;
        private UUID venueId;
        private UUID cityId;
        private String cityName;
        private Instant startTime;
        private Instant endTime;
        private Show.ShowStatus status;
        private BigDecimal basePrice;
        private String language;
        private Boolean is3D;
        private Boolean isAtmos;
        private Integer availableSeats;
        private Integer totalSeats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowListResponse {
        private UUID id;
        private Instant startTime;
        private Instant endTime;
        private Show.ShowStatus status;
        private BigDecimal basePrice;
        private String language;
        private Boolean is3D;
        private Boolean isAtmos;
        private String screenName;
        private Screen.ScreenType screenType;
        private Integer availableSeats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowAvailabilityResponse {
        private UUID showId;
        private UUID screenId;
        private String screenName;
        private Integer totalSeats;
        private Integer availableSeats;
        private List<SeatAvailability> seats;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatAvailability {
        private UUID seatId;
        private String rowLabel;
        private Integer seatNumber;
        private String displayLabel;
        private Seat.SeatType seatType;
        private SeatInventory.SeatStatus status;
        private BigDecimal price;
        private UUID holdId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateShowRequest {
        @NotNull private UUID movieId;
        @NotNull private UUID screenId;
        @NotNull private Instant startTime;
        @NotNull private Instant endTime;
        @NotNull private BigDecimal basePrice;
        @Size(max = 50) private String language;
        private Boolean is3D;
        private Boolean isAtmos;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateShowRequest {
        private Instant startTime;
        private Instant endTime;
        private BigDecimal basePrice;
        @Size(max = 50) private String language;
        private Boolean is3D;
        private Boolean isAtmos;
        private Show.ShowStatus status;
    }
}