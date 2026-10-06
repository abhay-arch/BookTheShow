package com.bookmyshow.catalog.dto;

import com.bookmyshow.catalog.entity.City;
import com.bookmyshow.catalog.entity.Venue;
import com.bookmyshow.catalog.entity.Screen;
import com.bookmyshow.catalog.entity.Seat;
import com.bookmyshow.catalog.entity.Movie;
import com.bookmyshow.catalog.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CatalogDtos {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityResponse {
        private UUID id;
        private String name;
        private String timezone;
        private Boolean isActive;
        
        public static CityResponse from(City city) {
            return CityResponse.builder()
                    .id(city.getId())
                    .name(city.getName())
                    .timezone(city.getTimezone())
                    .isActive(city.getIsActive())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenueResponse {
        private UUID id;
        private UUID cityId;
        private String cityName;
        private String name;
        private String address;
        private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String amenities;
        private Boolean isActive;
        private Integer screenCount;
        
        public static VenueResponse from(Venue venue, String cityName, Integer screenCount) {
            return VenueResponse.builder()
                    .id(venue.getId())
                    .cityId(venue.getCityId())
                    .cityName(cityName)
                    .name(venue.getName())
                    .address(venue.getAddress())
                    .description(venue.getDescription())
                    .latitude(venue.getLatitude())
                    .longitude(venue.getLongitude())
                    .amenities(venue.getAmenities())
                    .isActive(venue.getIsActive())
                    .screenCount(screenCount)
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenResponse {
        private UUID id;
        private UUID venueId;
        private String venueName;
        private String name;
        private Screen.ScreenType screenType;
        private Integer totalRows;
        private Integer totalColumns;
        private Integer totalSeats;
        private Boolean isActive;
        
        public static ScreenResponse from(Screen screen, String venueName) {
            return ScreenResponse.builder()
                    .id(screen.getId())
                    .venueId(screen.getVenueId())
                    .venueName(venueName)
                    .name(screen.getName())
                    .screenType(screen.getScreenType())
                    .totalRows(screen.getTotalRows())
                    .totalColumns(screen.getTotalColumns())
                    .totalSeats(screen.getTotalSeats())
                    .isActive(screen.getIsActive())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatResponse {
        private UUID id;
        private UUID screenId;
        private String rowLabel;
        private Integer seatNumber;
        private Seat.SeatType seatType;
        private Boolean isAccessible;
        private Boolean isActive;
        private String displayLabel;
        
        public static SeatResponse from(Seat seat) {
            return SeatResponse.builder()
                    .id(seat.getId())
                    .screenId(seat.getScreenId())
                    .rowLabel(seat.getRowLabel())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType())
                    .isAccessible(seat.getIsAccessible())
                    .isActive(seat.getIsActive())
                    .displayLabel(seat.getDisplayLabel())
                    .build();
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScreenLayoutResponse {
        private UUID screenId;
        private String screenName;
        private Integer totalRows;
        private Integer totalColumns;
        private List<SeatResponse> seats;
    }
    
    // Admin DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCityRequest {
        @NotBlank @Size(max = 100) private String name;
        @NotBlank @Size(max = 50) private String timezone;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateVenueRequest {
        @NotNull private UUID cityId;
        @NotBlank @Size(max = 200) private String name;
        @NotBlank @Size(max = 500) private String address;
        @Size(max = 1000) private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
        @Size(max = 500) private String amenities;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateScreenRequest {
        @NotNull private UUID venueId;
        @NotBlank @Size(max = 100) private String name;
        @NotNull private Screen.ScreenType screenType;
        @NotNull private Integer totalRows;
        @NotNull private Integer totalColumns;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSeatRequest {
        @NotNull private UUID screenId;
        @NotBlank @Size(max = 10) private String rowLabel;
        @NotNull private Integer seatNumber;
        @NotNull private Seat.SeatType seatType;
        private Boolean isAccessible;
    }
}