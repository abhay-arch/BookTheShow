package com.bookmyshow.booking.service;

import com.bookmyshow.booking.dto.BookingDtos;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.entity.SeatInventory;
import com.bookmyshow.booking.repository.BookingRepository;
import com.bookmyshow.booking.repository.BookingSeatRepository;
import com.bookmyshow.booking.repository.SeatInventoryRepository;
import com.bookmyshow.show.entity.Show;
import com.bookmyshow.show.repository.ShowRepository;
import com.bookmyshow.common.entity.OutboxEvent;
import com.bookmyshow.common.exception.BusinessException;
import com.bookmyshow.common.exception.ErrorCode;
import com.bookmyshow.common.repository.OutboxEventRepository;
import com.bookmyshow.user.entity.User;
import com.bookmyshow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceConcurrencyTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    
    @Mock
    private SeatInventoryRepository seatInventoryRepository;
    
    @Mock
    private ShowRepository showRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OutboxEventRepository outboxEventRepository;
    
    @InjectMocks
    private BookingService bookingService;
    
    private UUID userId;
    private UUID showId;
    private UUID seatId;
    private Show show;
    private SeatInventory seatInventory;
    private User user;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        showId = UUID.randomUUID();
        seatId = UUID.randomUUID();
        
        show = Show.builder()
                .id(showId)
                .status(Show.ShowStatus.ON_SALE)
                .startTime(Instant.now().plus(Duration.ofHours(2)))
                .build();
        
        seatInventory = SeatInventory.builder()
                .id(UUID.randomUUID())
                .showId(showId)
                .seatId(seatId)
                .status(SeatInventory.SeatStatus.AVAILABLE)
                .price(new BigDecimal("200.00"))
                .build();
        
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .status(User.UserStatus.ACTIVE)
                .build();
        
        ReflectionTestUtils.setField(bookingService, "holdDurationMinutes", 10);
        ReflectionTestUtils.setField(bookingService, "maxSeatsPerBooking", 10);
    }
    
    @Test
    void shouldHoldSeatsSuccessfully() {
        BookingDtos.HoldSeatsRequest request = BookingDtos.HoldSeatsRequest.builder()
                .showId(showId)
                .seatIds(List.of(seatId))
                .idempotencyKey("test-key-1")
                .build();
        
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), eq(List.of(seatId))))
                .thenReturn(List.of(seatInventory));
        when(bookingRepository.findByIdempotencyKey("test-key-1")).thenReturn(Optional.empty());
        when(seatInventoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        BookingDtos.HoldResponse response = bookingService.holdSeats(userId, request);
        
        assertThat(response).isNotNull();
        assertThat(response.getHoldId()).isNotNull();
        assertThat(response.getSeats()).hasSize(1);
        verify(seatInventoryRepository).saveAll(anyList());
    }
    
    @Test
    void shouldThrowExceptionWhenSeatAlreadyHeld() {
        seatInventory.setStatus(SeatInventory.SeatStatus.HELD);
        seatInventory.setHoldExpiresAt(Instant.now().plus(Duration.ofMinutes(5)));
        
        BookingDtos.HoldSeatsRequest request = BookingDtos.HoldSeatsRequest.builder()
                .showId(showId)
                .seatIds(List.of(seatId))
                .idempotencyKey("test-key-2")
                .build();
        
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), eq(List.of(seatId))))
                .thenReturn(List.of(seatInventory));
        
        assertThatThrownBy(() -> bookingService.holdSeats(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_ALREADY_HELD);
    }
    
    @Test
    void shouldAllowHoldAfterExpiredHold() {
        seatInventory.setStatus(SeatInventory.SeatStatus.HELD);
        seatInventory.setHoldExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));
        
        BookingDtos.HoldSeatsRequest request = BookingDtos.HoldSeatsRequest.builder()
                .showId(showId)
                .seatIds(List.of(seatId))
                .idempotencyKey("test-key-3")
                .build();
        
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), eq(List.of(seatId))))
                .thenReturn(List.of(seatInventory));
        when(bookingRepository.findByIdempotencyKey("test-key-3")).thenReturn(Optional.empty());
        when(seatInventoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        BookingDtos.HoldResponse response = bookingService.holdSeats(userId, request);
        
        assertThat(response).isNotNull();
    }
    
    @Test
    void shouldHandleConcurrentHoldRequests() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Setup mocks for concurrent access
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        
        // Each thread gets its own seat inventory mock
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    UUID threadSeatId = UUID.randomUUID();
                    SeatInventory threadInventory = SeatInventory.builder()
                            .id(UUID.randomUUID())
                            .showId(showId)
                            .seatId(threadSeatId)
                            .status(SeatInventory.SeatStatus.AVAILABLE)
                            .price(new BigDecimal("200.00"))
                            .build();
                    
                    when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), eq(List.of(threadSeatId))))
                            .thenReturn(List.of(threadInventory));
                    when(seatInventoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
                    when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
                    when(bookingSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
                    when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
                    
                    BookingDtos.HoldSeatsRequest request = BookingDtos.HoldSeatsRequest.builder()
                            .showId(showId)
                            .seatIds(List.of(threadSeatId))
                            .idempotencyKey("test-key-" + index)
                            .build();
                    
                    bookingService.holdSeats(userId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // All should succeed since each thread uses different seats
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isEqualTo(0);
    }
    
    @Test
    void shouldPreventDoubleBookingSameSeat() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // All threads try to book the SAME seat
                    when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), eq(List.of(seatId))))
                            .thenReturn(List.of(seatInventory));
                    when(seatInventoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
                    when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
                    when(bookingSeatRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
                    when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
                    
                    BookingDtos.HoldSeatsRequest request = BookingDtos.HoldSeatsRequest.builder()
                            .showId(showId)
                            .seatIds(List.of(seatId))
                            .idempotencyKey("test-key-concurrent-" + index)
                            .build();
                    
                    bookingService.holdSeats(userId, request);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (e.getErrorCode() == ErrorCode.SEAT_ALREADY_HELD) {
                        failureCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Only one should succeed, rest should fail with SEAT_ALREADY_HELD
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(threadCount - 1);
    }
    
    @Test
    void shouldReleaseExpiredHolds() {
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .status(Booking.BookingStatus.PENDING_PAYMENT)
                .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                .build();
        
        when(bookingRepository.findExpiredPendingBookings(any(Instant.class)))
                .thenReturn(List.of(booking));
        when(seatInventoryRepository.findByShowIdAndSeatIdsForUpdate(eq(showId), anyList()))
                .thenReturn(List.of(seatInventory));
        when(seatInventoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        bookingService.releaseExpiredHolds();
        
        assertThat(seatInventory.getStatus()).isEqualTo(SeatInventory.SeatStatus.AVAILABLE);
        assertThat(seatInventory.getHoldId()).isNull();
        assertThat(booking.getStatus()).isEqualTo(Booking.BookingStatus.EXPIRED);
        verify(bookingRepository).save(booking);
    }
}