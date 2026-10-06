package com.bookmyshow.show.service;

import com.bookmyshow.show.dto.ShowDtos;
import com.bookmyshow.show.entity.Show;
import com.bookmyshow.show.repository.ShowRepository;
import com.bookmyshow.catalog.entity.Screen;
import com.bookmyshow.catalog.entity.Movie;
import com.bookmyshow.catalog.repository.ScreenRepository;
import com.bookmyshow.catalog.repository.MovieRepository;
import com.bookmyshow.booking.entity.SeatInventory;
import com.bookmyshow.booking.repository.SeatInventoryRepository;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowService {
    
    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    
    @Cacheable(value = "shows", key = "#movieId + ':' + #cityId + ':' + #now.toEpochMilli()", unless = "#result.isEmpty()")
    public List<ShowDtos.ShowListResponse> getShowsForMovie(UUID movieId, UUID cityId) {
        Instant now = Instant.now();
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        
        List<Show> shows = showRepository.findShowsForMovie(movieId, cityId, now);
        
        return shows.stream()
                .map(show -> mapToListResponse(show, movie.getTitle()))
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "shows", key = "#cityId + ':' + #from.toEpochMilli() + ':' + #to.toEpochMilli()", unless = "#result.isEmpty()")
    public List<ShowDtos.ShowListResponse> getShowsInCity(UUID cityId, Instant from, Instant to) {
        List<Show> shows = showRepository.findShowsInCity(cityId, from, to);
        return shows.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }
    
    public ShowDtos.ShowResponse getShowById(UUID showId) {
        Show show = showRepository.findBookableShowById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        
        return mapToResponse(show);
    }
    
    public ShowDtos.ShowAvailabilityResponse getShowAvailability(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        
        Screen screen = screenRepository.findById(show.getScreenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
        
        List<SeatInventory> inventory = seatInventoryRepository.findByShowId(showId);
        
        List<ShowDtos.SeatAvailability> seatAvailabilities = inventory.stream()
                .map(inv -> {
                    // Would need to fetch seat details
                    return ShowDtos.SeatAvailability.builder()
                            .seatId(inv.getSeatId())
                            .seatType(null) // Would need seat lookup
                            .status(inv.getStatus())
                            .price(inv.getPrice())
                            .holdId(inv.getHoldId())
                            .build();
                })
                .collect(Collectors.toList());
        
        long availableCount = inventory.stream()
                .filter(inv -> inv.getStatus() == SeatInventory.SeatStatus.AVAILABLE)
                .count();
        
        return ShowDtos.ShowAvailabilityResponse.builder()
                .showId(show.getId())
                .screenId(screen.getId())
                .screenName(screen.getName())
                .totalSeats(inventory.size())
                .availableSeats((int) availableCount)
                .seats(seatAvailabilities)
                .build();
    }
    
    private ShowDtos.ShowResponse mapToResponse(Show show) {
        // Would need to fetch related entities
        return ShowDtos.ShowResponse.builder()
                .id(show.getId())
                .movieId(show.getMovieId())
                .screenId(show.getScreenId())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .status(show.getStatus())
                .basePrice(show.getBasePrice())
                .language(show.getLanguage())
                .is3D(show.getIs3D())
                .isAtmos(show.getIsAtmos())
                .build();
    }
    
    private ShowDtos.ShowListResponse mapToListResponse(Show show, String movieTitle) {
        Screen screen = screenRepository.findById(show.getScreenId()).orElse(null);
        return ShowDtos.ShowListResponse.builder()
                .id(show.getId())
                .startTime(show.getStartTime())
                .endTime(show.getEndTime())
                .status(show.getStatus())
                .basePrice(show.getBasePrice())
                .language(show.getLanguage())
                .is3D(show.getIs3D())
                .isAtmos(show.getIsAtmos())
                .screenName(screen != null ? screen.getName() : "")
                .screenType(screen != null ? screen.getScreenType() : null)
                .availableSeats(0) // Would need to compute
                .build();
    }
    
    private ShowDtos.ShowListResponse mapToListResponse(Show show) {
        return mapToListResponse(show, "");
    }
    
    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowDtos.ShowResponse createShow(ShowDtos.CreateShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MOVIE_NOT_FOUND));
        
        Screen screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
        
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "End time must be after start time");
        }
        
        // Check for overlapping shows on the same screen
        List<Show> overlapping = showRepository.findByScreenIdAndStartTimeBetween(
                request.getScreenId(),
                request.getStartTime().minus(Duration.ofMinutes(movie.getDurationMinutes() + 30)),
                request.getEndTime().plus(Duration.ofMinutes(30)));
        
        if (!overlapping.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Show overlaps with existing show on this screen");
        }
        
        Show show = Show.builder()
                .id(UUID.randomUUID())
                .movieId(request.getMovieId())
                .screenId(request.getScreenId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(Show.ShowStatus.SCHEDULED)
                .basePrice(request.getBasePrice())
                .language(request.getLanguage())
                .is3D(request.getIs3D() != null ? request.getIs3D() : false)
                .isAtmos(request.getIsAtmos() != null ? request.getIsAtmos() : false)
                .build();
        
        show = showRepository.save(show);
        
        // Initialize seat inventory
        initializeSeatInventory(show, screen);
        
        log.info("Created show: {} for movie: {} on screen: {}", show.getId(), movie.getTitle(), screen.getName());
        return mapToResponse(show);
    }
    
    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public ShowDtos.ShowResponse updateShow(UUID showId, ShowDtos.UpdateShowRequest request) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        
        if (request.getStartTime() != null) show.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) show.setEndTime(request.getEndTime());
        if (request.getBasePrice() != null) show.setBasePrice(request.getBasePrice());
        if (request.getLanguage() != null) show.setLanguage(request.getLanguage());
        if (request.getIs3D() != null) show.setIs3D(request.getIs3D());
        if (request.getIsAtmos() != null) show.setIsAtmos(request.getIsAtmos());
        if (request.getStatus() != null) show.setStatus(request.getStatus());
        
        show = showRepository.save(show);
        log.info("Updated show: {}", show.getId());
        return mapToResponse(show);
    }
    
    @Transactional
    @CacheEvict(value = "shows", allEntries = true)
    public void deleteShow(UUID showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        
        show.setStatus(Show.ShowStatus.CANCELLED);
        showRepository.save(show);
        log.info("Cancelled show: {}", showId);
    }
    
    private void initializeSeatInventory(Show show, Screen screen) {
        // This would typically be done by a separate job or during screen setup
        // For now, we assume seats are already created and inventory is initialized separately
    }
}