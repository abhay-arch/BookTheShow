package com.bookmyshow.booking.service;

import com.bookmyshow.booking.dto.BookingDtos;
import com.bookmyshow.booking.entity.Booking;
import com.bookmyshow.booking.entity.BookingSeat;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    
    @Value("${bookmyshow.booking.seat-hold-duration-minutes:10}")
    private int holdDurationMinutes;
    
    @Value("${bookmyshow.booking.max-seats-per-booking:10}")
    private int maxSeatsPerBooking;
    
    @Transactional
    public BookingDtos.HoldResponse holdSeats(UUID userId, BookingDtos.HoldSeatsRequest request) {
        // Validate idempotency
        if (request.getIdempotencyKey() != null) {
            bookingRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
                    });
        }
        
        // Validate show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
        
        if (show.getStatus() != Show.ShowStatus.ON_SALE && show.getStatus() != Show.ShowStatus.SOLD_OUT) {
            throw new BusinessException(ErrorCode.SHOW_NOT_BOOKABLE);
        }
        
        // Validate seat count
        if (request.getSeatIds().size() > maxSeatsPerBooking) {
            throw new BusinessException(ErrorCode.MAX_SEATS_EXCEEDED);
        }
        
        // Lock and validate seats using pessimistic locking
        List<SeatInventory> seatInventories = seatInventoryRepository
                .findByShowIdAndSeatIdsForUpdate(request.getShowId(), request.getSeatIds());
        
        if (seatInventories.size() != request.getSeatIds().size()) {
            throw new BusinessException(ErrorCode.SEAT_NOT_FOUND, "One or more seats not found");
        }
        
        // Check all seats are available
        for (SeatInventory inventory : seatInventories) {
            if (inventory.getStatus() != SeatInventory.SeatStatus.AVAILABLE) {
                if (inventory.getStatus() == SeatInventory.SeatStatus.HELD) {
                    if (inventory.getHoldExpiresAt() != null && inventory.getHoldExpiresAt().isAfter(Instant.now())) {
                        throw new BusinessException(ErrorCode.SEAT_ALREADY_HELD);
                    }
                    // Expired hold - will be released below
                } else {
                    throw new BusinessException(ErrorCode.SEAT_ALREADY_BOOKED);
                }
            }
        }
        
        // Create hold ID
        UUID holdId = UUID.randomUUID();
        Instant holdExpiresAt = Instant.now().plusSeconds(holdDurationMinutes * 60L);
        
        // Update seat inventory to HELD
        for (SeatInventory inventory : seatInventories) {
            inventory.setStatus(SeatInventory.SeatStatus.HELD);
            inventory.setHoldId(holdId);
            inventory.setHoldExpiresAt(holdExpiresAt);
        }
        seatInventoryRepository.saveAll(seatInventories);
        
        // Create pending booking
        BigDecimal totalAmount = seatInventories.stream()
                .map(SeatInventory::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(request.getShowId())
                .status(Booking.BookingStatus.PENDING_PAYMENT)
                .totalAmount(totalAmount)
                .seatCount(request.getSeatIds().size())
                .idempotencyKey(request.getIdempotencyKey())
                .expiresAt(holdExpiresAt)
                .build();
        
        booking = bookingRepository.save(booking);
        
        final Booking savedBooking = booking;
        
        // Create booking seats
        List<BookingSeat> bookingSeats = seatInventories.stream()
                .map(inv -> BookingSeat.builder()
                        .bookingId(savedBooking.getId())
                        .seatId(inv.getSeatId())
                        .showId(request.getShowId())
                        .price(inv.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        bookingSeatRepository.saveAll(bookingSeats);
        
        // Publish booking created event via outbox
        publishBookingCreatedEvent(booking, seatInventories);
        
        log.info("Created hold {} for booking {} with {} seats", holdId, booking.getId(), request.getSeatIds().size());
        
        return buildHoldResponse(holdId, booking, seatInventories, holdExpiresAt);
    }
    
    @Transactional
    public BookingDtos.BookingResponse confirmBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "Booking is not in pending payment state");
        }
        
        if (booking.getExpiresAt() != null && booking.getExpiresAt().isBefore(Instant.now())) {
            // Release seats and mark booking as expired
            releaseSeatsForBooking(booking);
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new BusinessException(ErrorCode.HOLD_EXPIRED);
        }
        
        // Confirm booking - seats are already held, just update status
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmedAt(Instant.now());
        booking.setExpiresAt(null);
        bookingRepository.save(booking);
        
        // Update seat inventory to BOOKED
        List<SeatInventory> seatInventories = seatInventoryRepository
                .findByShowIdAndSeatIdsForUpdate(booking.getShowId(), 
                        bookingSeatRepository.findByBookingId(bookingId).stream()
                                .map(BookingSeat::getSeatId)
                                .collect(Collectors.toList()));
        
        for (SeatInventory inventory : seatInventories) {
            if (inventory.getHoldId() != null && inventory.getHoldId().equals(booking.getId())) {
                inventory.setStatus(SeatInventory.SeatStatus.BOOKED);
                inventory.setBookingId(booking.getId());
                inventory.setHoldId(null);
                inventory.setHoldExpiresAt(null);
            }
        }
        seatInventoryRepository.saveAll(seatInventories);
        
        // Publish booking confirmed event
        publishBookingConfirmedEvent(booking);
        
        log.info("Confirmed booking: {}", bookingId);
        return buildBookingResponse(booking);
    }
    
    @Transactional
    public void releaseExpiredHolds() {
        Instant now = Instant.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingBookings(now);
        
        for (Booking booking : expiredBookings) {
            releaseSeatsForBooking(booking);
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            log.info("Released expired hold for booking: {}", booking.getId());
        }
    }
    
    private void releaseSeatsForBooking(Booking booking) {
        List<SeatInventory> seatInventories = seatInventoryRepository
                .findByShowIdAndSeatIdsForUpdate(booking.getShowId(),
                        bookingSeatRepository.findByBookingId(booking.getId()).stream()
                                .map(BookingSeat::getSeatId)
                                .collect(Collectors.toList()));
        
        for (SeatInventory inventory : seatInventories) {
            if (inventory.getHoldId() != null && inventory.getHoldId().equals(booking.getId())) {
                inventory.setStatus(SeatInventory.SeatStatus.AVAILABLE);
                inventory.setHoldId(null);
                inventory.setHoldExpiresAt(null);
            }
        }
        seatInventoryRepository.saveAll(seatInventories);
    }
    
    @Transactional
    public BookingDtos.BookingResponse cancelBooking(UUID bookingId, UUID userId, BookingDtos.CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "Booking already cancelled");
        }
        
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            // Check if cancellation is allowed (e.g., before show time)
            Show show = showRepository.findById(booking.getShowId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHOW_NOT_FOUND));
            
            if (show.getStartTime().isBefore(Instant.now().plus(Duration.ofHours(2)))) {
                throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED, "Cannot cancel within 2 hours of show");
            }
        }
        
        // Release seats if still held
        if (booking.getStatus() == Booking.BookingStatus.PENDING_PAYMENT) {
            releaseSeatsForBooking(booking);
        }
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(request.getReason());
        bookingRepository.save(booking);
        
        // Publish cancellation event
        publishBookingCancelledEvent(booking);
        
        log.info("Cancelled booking: {}", bookingId);
        return buildBookingResponse(booking);
    }
    
    public BookingDtos.BookingResponse getBookingById(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        
        return buildBookingResponse(booking);
    }
    
    public List<BookingDtos.BookingListResponse> getUserBookings(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }
    
    private BookingDtos.HoldResponse buildHoldResponse(UUID holdId, Booking booking, 
                                                      List<SeatInventory> seatInventories, 
                                                      Instant expiresAt) {
        // Would need to fetch seat details for response
        return BookingDtos.HoldResponse.builder()
                .holdId(holdId)
                .showId(booking.getShowId())
                .seats(List.of()) // Would be populated
                .expiresAt(expiresAt)
                .holdDurationMinutes(holdDurationMinutes)
                .build();
    }
    
    private BookingDtos.BookingResponse buildBookingResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        
        List<BookingDtos.BookedSeatResponse> seatResponses = bookingSeats.stream()
                .map(bs -> BookingDtos.BookedSeatResponse.builder()
                        .seatId(bs.getSeatId())
                        .price(bs.getPrice())
                        .build())
                .collect(Collectors.toList());
        
        return BookingDtos.BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .showId(booking.getShowId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .seatCount(booking.getSeatCount())
                .seats(seatResponses)
                .expiresAt(booking.getExpiresAt())
                .confirmedAt(booking.getConfirmedAt())
                .idempotencyKey(booking.getIdempotencyKey())
                .build();
    }
    
    private BookingDtos.BookingListResponse mapToListResponse(Booking booking) {
        return BookingDtos.BookingListResponse.builder()
                .id(booking.getId())
                .showId(booking.getShowId())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .seatCount(booking.getSeatCount())
                .createdAt(booking.getCreatedAt())
                .confirmedAt(booking.getConfirmedAt())
                .build();
    }
    
    private void publishBookingCreatedEvent(Booking booking, List<SeatInventory> seats) {
        // Create outbox event
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(booking.getId())
                .eventType("BookingCreated")
                .payload(buildBookingEventPayload(booking, seats))
                .build();
        outboxEventRepository.save(event);
    }
    
    private void publishBookingConfirmedEvent(Booking booking) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(booking.getId())
                .eventType("BookingConfirmed")
                .payload("{\"bookingId\":\"" + booking.getId() + "\",\"userId\":\"" + booking.getUserId() + "\"}")
                .build();
        outboxEventRepository.save(event);
    }
    
    private void publishBookingCancelledEvent(Booking booking) {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(booking.getId())
                .eventType("BookingCancelled")
                .payload("{\"bookingId\":\"" + booking.getId() + "\",\"userId\":\"" + booking.getUserId() + "\"}")
                .build();
        outboxEventRepository.save(event);
    }
    
    private String buildBookingEventPayload(Booking booking, List<SeatInventory> seats) {
        String seatIds = seats.stream()
                .map(s -> "\"" + s.getSeatId() + "\"")
                .collect(Collectors.joining(","));
        
        return String.format(
                "{\"bookingId\":\"%s\",\"userId\":\"%s\",\"showId\":\"%s\",\"seatIds\":[%s],\"amount\":%s}",
                booking.getId(), booking.getUserId(), booking.getShowId(), seatIds, booking.getTotalAmount()
        );
    }
}