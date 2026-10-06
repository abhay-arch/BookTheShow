package com.bookmyshow.common.kafka;

import com.bookmyshow.notification.service.NotificationService;
import com.bookmyshow.notification.dto.NotificationDtos;
import com.bookmyshow.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {
    
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "booking-events", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeBookingEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String payload = record.value();
            
            log.info("Received booking event: {} - {}", eventType, payload);
            
            switch (eventType) {
                case "BookingCreated" -> handleBookingCreated(payload);
                case "BookingConfirmed" -> handleBookingConfirmed(payload);
                case "BookingCancelled" -> handleBookingCancelled(payload);
                case "SeatHoldExpired" -> handleSeatHoldExpired(payload);
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process booking event: {}", record.value(), e);
            // Don't acknowledge - will be retried
        }
    }
    
    private void handleBookingCreated(String payload) {
        // Parse payload and send notification
        // In real implementation, use ObjectMapper
        UUID userId = extractUserId(payload);
        UUID bookingId = extractBookingId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.BOOKING_CONFIRMATION)
                .channel(Notification.Channel.EMAIL)
                .subject("Booking Hold Created")
                .content("Your seat hold has been created. Complete payment within 10 minutes.")
                .build());
    }
    
    private void handleBookingConfirmed(String payload) {
        UUID userId = extractUserId(payload);
        UUID bookingId = extractBookingId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.BOOKING_CONFIRMATION)
                .channel(Notification.Channel.EMAIL)
                .subject("Booking Confirmed")
                .content("Your booking has been confirmed. Enjoy the show!")
                .build());
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.BOOKING_CONFIRMATION)
                .channel(Notification.Channel.SMS)
                .subject("Booking Confirmed")
                .content("Booking confirmed. Show details in app.")
                .build());
    }
    
    private void handleBookingCancelled(String payload) {
        UUID userId = extractUserId(payload);
        UUID bookingId = extractBookingId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.BOOKING_CANCELLATION)
                .channel(Notification.Channel.EMAIL)
                .subject("Booking Cancelled")
                .content("Your booking has been cancelled.")
                .build());
    }
    
    private void handleSeatHoldExpired(String payload) {
        UUID userId = extractUserId(payload);
        UUID bookingId = extractBookingId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.SEAT_HOLD_EXPIRY)
                .channel(Notification.Channel.EMAIL)
                .subject("Seat Hold Expired")
                .content("Your seat hold has expired. Seats have been released.")
                .build());
    }
    
    private UUID extractUserId(String payload) {
        // Simplified - use ObjectMapper in real implementation
        return UUID.randomUUID();
    }
    
    private UUID extractBookingId(String payload) {
        return UUID.randomUUID();
    }
}