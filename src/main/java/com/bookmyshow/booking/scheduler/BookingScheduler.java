package com.bookmyshow.booking.scheduler;

import com.bookmyshow.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {
    
    private final BookingService bookingService;
    
    @Scheduled(fixedDelay = 60000) // Every minute
    public void releaseExpiredHolds() {
        log.debug("Running expired holds release job");
        try {
            bookingService.releaseExpiredHolds();
        } catch (Exception e) {
            log.error("Failed to release expired holds", e);
        }
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanupOldBookings() {
        log.debug("Running old bookings cleanup job");
        // Clean up old cancelled/expired bookings if needed
    }
}