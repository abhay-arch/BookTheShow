package com.bookmyshow.integration;

import com.bookmyshow.booking.dto.BookingDtos;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.entity.SeatInventory;
import com.bookmyshow.booking.repository.BookingRepository;
import com.bookmyshow.booking.repository.SeatInventoryRepository;
import com.bookmyshow.catalog.entity.City;
import com.bookmyshow.catalog.entity.Screen;
import com.bookmyshow.catalog.entity.Seat;
import com.bookmyshow.catalog.entity.Venue;
import com.bookmyshow.catalog.repository.CityRepository;
import com.bookmyshow.catalog.repository.ScreenRepository;
import com.bookmyshow.catalog.repository.SeatRepository;
import com.bookmyshow.catalog.repository.VenueRepository;
import com.bookmyshow.show.entity.Show;
import com.bookmyshow.show.repository.ShowRepository;
import com.bookmyshow.user.entity.User;
import com.bookmyshow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@Transactional
class BookingIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookmyshow")
            .withUsername("postgres")
            .withPassword("postgres");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CityRepository cityRepository;
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Autowired
    private ScreenRepository screenRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ShowRepository showRepository;
    
    @Autowired
    private SeatInventoryRepository seatInventoryRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    private User user;
    private Show show;
    private List<Seat> seats;
    
    @BeforeEach
    void setUp() {
        // Create test data
        City city = City.builder()
                .id(UUID.randomUUID())
                .name("Hyderabad")
                .timezone("Asia/Kolkata")
                .isActive(true)
                .build();
        city = cityRepository.save(city);
        
        Venue venue = Venue.builder()
                .id(UUID.randomUUID())
                .cityId(city.getId())
                .name("PVR Forum Mall")
                .address("Forum Mall, Kukatpally")
                .isActive(true)
                .build();
        venue = venueRepository.save(venue);
        
        Screen screen = Screen.builder()
                .id(UUID.randomUUID())
                .venueId(venue.getId())
                .name("Screen 1")
                .screenType(Screen.ScreenType.STANDARD)
                .totalRows(5)
                .totalColumns(10)
                .totalSeats(50)
                .isActive(true)
                .build();
        screen = screenRepository.save(screen);
        
        // Create seats
        seats = List.of(
                createSeat(screen, "A", 1, Seat.SeatType.STANDARD),
                createSeat(screen, "A", 2, Seat.SeatType.STANDARD),
                createSeat(screen, "A", 3, Seat.SeatType.PREMIUM)
        );
        seats = seatRepository.saveAll(seats);
        
        // Create show
        show = Show.builder()
                .id(UUID.randomUUID())
                .movieId(UUID.randomUUID())
                .screenId(screen.getId())
                .startTime(Instant.now().plus(Duration.ofHours(2)))
                .endTime(Instant.now().plus(Duration.ofHours(5)))
                .status(Show.ShowStatus.ON_SALE)
                .basePrice(new BigDecimal("200.00"))
                .build();
        show = showRepository.save(show);
        
        // Initialize seat inventory
        for (Seat seat : seats) {
            SeatInventory inventory = SeatInventory.builder()
                    .id(UUID.randomUUID())
                    .showId(show.getId())
                    .seatId(seat.getId())
                    .status(SeatInventory.SeatStatus.AVAILABLE)
                    .price(new BigDecimal("200.00"))
                    .build();
            seatInventoryRepository.save(inventory);
        }
        
        // Create user
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phone("+919876543210")
                .passwordHash("$2a$12$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .status(User.UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);
    }
    
    private Seat createSeat(Screen screen, String row, int number, Seat.SeatType type) {
        return Seat.builder()
                .id(UUID.randomUUID())
                .screenId(screen.getId())
                .rowLabel(row)
                .seatNumber(number)
                .seatType(type)
                .isActive(true)
                .build();
    }
    
    @Test
    void shouldCreateBookingSuccessfully() {
        // This would test the full booking flow via the service
        // For now, verify the test data setup
        assertThat(seats).hasSize(3);
        assertThat(show.getStatus()).isEqualTo(Show.ShowStatus.ON_SALE);
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
        
        // Verify seat inventory created
        List<SeatInventory> inventory = seatInventoryRepository.findByShowId(show.getId());
        assertThat(inventory).hasSize(3);
        assertThat(inventory).allMatch(inv -> inv.getStatus() == SeatInventory.SeatStatus.AVAILABLE);
    }
    
    @Test
    void shouldFindAvailableSeats() {
        List<SeatInventory> available = seatInventoryRepository.findByShowIdAndStatus(
                show.getId(), SeatInventory.SeatStatus.AVAILABLE);
        
        assertThat(available).hasSize(3);
    }
    
    @Test
    void shouldLockSeatsForUpdate() {
        List<SeatInventory> locked = seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(
                show.getId(), seats.stream().map(Seat::getId).toList());
        
        assertThat(locked).hasSize(3);
    }
}