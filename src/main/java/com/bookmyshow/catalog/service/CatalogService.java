package com.bookmyshow.catalog.service;

import com.bookmyshow.catalog.dto.CatalogDtos;
import com.bookmyshow.catalog.entity.City;
import com.bookmyshow.catalog.entity.Venue;
import com.bookmyshow.catalog.entity.Screen;
import com.bookmyshow.catalog.entity.Seat;
import com.bookmyshow.catalog.repository.CityRepository;
import com.bookmyshow.catalog.repository.VenueRepository;
import com.bookmyshow.catalog.repository.ScreenRepository;
import com.bookmyshow.catalog.repository.SeatRepository;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {
    
    private final CityRepository cityRepository;
    private final VenueRepository venueRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    
    // City operations
    public List<CatalogDtos.CityResponse> getAllCities() {
        return cityRepository.findByIsActiveTrue().stream()
                .map(CatalogDtos.CityResponse::from)
                .collect(Collectors.toList());
    }
    
    public CatalogDtos.CityResponse getCityById(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
        return CatalogDtos.CityResponse.from(city);
    }
    
    // Venue operations
    @Cacheable(value = "venues", key = "#cityId", unless = "#result.isEmpty()")
    public List<CatalogDtos.VenueResponse> getVenuesByCity(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
        
        List<Venue> venues = venueRepository.findByCityIdAndIsActiveTrue(cityId);
        return venues.stream()
                .map(venue -> {
                    long screenCount = screenRepository.countByVenueIdAndIsActiveTrue(venue.getId());
                    return CatalogDtos.VenueResponse.from(venue, city.getName(), (int) screenCount);
                })
                .collect(Collectors.toList());
    }
    
    public CatalogDtos.VenueResponse getVenueById(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        
        City city = cityRepository.findById(venue.getCityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
        
        long screenCount = screenRepository.countByVenueIdAndIsActiveTrue(venueId);
        return CatalogDtos.VenueResponse.from(venue, city.getName(), (int) screenCount);
    }
    
    // Screen operations
    @Cacheable(value = "screens", key = "#venueId", unless = "#result.isEmpty()")
    public List<CatalogDtos.ScreenResponse> getScreensByVenue(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        
        return screenRepository.findByVenueIdAndIsActiveTrue(venueId).stream()
                .map(screen -> CatalogDtos.ScreenResponse.from(screen, venue.getName()))
                .collect(Collectors.toList());
    }
    
    public CatalogDtos.ScreenResponse getScreenById(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
        
        Venue venue = venueRepository.findById(screen.getVenueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        
        return CatalogDtos.ScreenResponse.from(screen, venue.getName());
    }
    
    // Seat operations
    @Cacheable(value = "seats", key = "#screenId", unless = "#result == null")
    public CatalogDtos.ScreenLayoutResponse getScreenLayout(UUID screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
        
        List<Seat> seats = seatRepository.findByScreenIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(screenId);
        
        List<CatalogDtos.SeatResponse> seatResponses = seats.stream()
                .map(CatalogDtos.SeatResponse::from)
                .collect(Collectors.toList());
        
        return CatalogDtos.ScreenLayoutResponse.builder()
                .screenId(screen.getId())
                .screenName(screen.getName())
                .totalRows(screen.getTotalRows())
                .totalColumns(screen.getTotalColumns())
                .seats(seatResponses)
                .build();
    }
    
    // Admin operations
    @Transactional
    public CatalogDtos.CityResponse createCity(CatalogDtos.CreateCityRequest request) {
        if (cityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "City name already exists");
        }
        
        City city = City.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .timezone(request.getTimezone())
                .isActive(true)
                .build();
        
        city = cityRepository.save(city);
        log.info("Created city: {}", city.getName());
        return CatalogDtos.CityResponse.from(city);
    }
    
    @Transactional
    public CatalogDtos.VenueResponse createVenue(CatalogDtos.CreateVenueRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CITY_NOT_FOUND));
        
        Venue venue = Venue.builder()
                .id(UUID.randomUUID())
                .cityId(request.getCityId())
                .name(request.getName())
                .address(request.getAddress())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .amenities(request.getAmenities())
                .isActive(true)
                .build();
        
        venue = venueRepository.save(venue);
        log.info("Created venue: {} in city: {}", venue.getName(), city.getName());
        return CatalogDtos.VenueResponse.from(venue, city.getName(), 0);
    }
    
    @Transactional
    public CatalogDtos.ScreenResponse createScreen(CatalogDtos.CreateScreenRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        
        Screen screen = Screen.builder()
                .id(UUID.randomUUID())
                .venueId(request.getVenueId())
                .name(request.getName())
                .screenType(request.getScreenType())
                .totalRows(request.getTotalRows())
                .totalColumns(request.getTotalColumns())
                .totalSeats(request.getTotalRows() * request.getTotalColumns())
                .isActive(true)
                .build();
        
        screen = screenRepository.save(screen);
        log.info("Created screen: {} in venue: {}", screen.getName(), venue.getName());
        return CatalogDtos.ScreenResponse.from(screen, venue.getName());
    }
    
    @Transactional
    public CatalogDtos.SeatResponse createSeat(CatalogDtos.CreateSeatRequest request) {
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
        
        if (seatRepository.findByScreenIdAndRowLabelAndSeatNumber(
                request.getScreenId(), request.getRowLabel(), request.getSeatNumber()).isPresent()) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_SELECTION, "Seat already exists");
        }
        
        Seat seat = Seat.builder()
                .id(UUID.randomUUID())
                .screenId(request.getScreenId())
                .rowLabel(request.getRowLabel())
                .seatNumber(request.getSeatNumber())
                .seatType(request.getSeatType())
                .isAccessible(request.getIsAccessible() != null ? request.getIsAccessible() : false)
                .isActive(true)
                .build();
        
        seat = seatRepository.save(seat);
        
        // Update screen total seats count
        screen.setTotalSeats(screen.getTotalSeats() + 1);
        screenRepository.save(screen);
        
        log.info("Created seat: {} in screen: {}", seat.getDisplayLabel(), screen.getName());
        return CatalogDtos.SeatResponse.from(seat);
    }
}