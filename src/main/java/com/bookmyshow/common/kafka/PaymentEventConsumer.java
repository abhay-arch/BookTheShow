package com.bookmyshow.common.kafka;

import com.bookmyshow.booking.service.BookingService;
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
public class PaymentEventConsumer {
    
    private final BookingService bookingService;
    private final NotificationService notificationService;
    
    @KafkaListener(topics = "payment-events", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentEvent(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String payload = record.value();
            
            log.info("Received payment event: {} - {}", eventType, payload);
            
            switch (eventType) {
                case "PaymentSucceeded" -> handlePaymentSucceeded(payload);
                case "PaymentFailed" -> handlePaymentFailed(payload);
                case "PaymentRefunded" -> handlePaymentRefunded(payload);
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", record.value(), e);
        }
    }
    
    private void handlePaymentSucceeded(String payload) {
        UUID bookingId = extractBookingId(payload);
        
        // Booking confirmation is handled by webhook, but we can send notification here too
        UUID userId = extractUserId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.PAYMENT_SUCCESS)
                .channel(Notification.Channel.EMAIL)
                .subject("Payment Successful")
                .content("Your payment has been processed successfully.")
                .build());
    }
    
    private void handlePaymentFailed(String payload) {
        UUID bookingId = extractBookingId(payload);
        UUID userId = extractUserId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.PAYMENT_FAILED)
                .channel(Notification.Channel.EMAIL)
                .subject("Payment Failed")
                .content("Your payment could not be processed. Please try again.")
                .build());
    }
    
    private void handlePaymentRefunded(String payload) {
        UUID bookingId = extractBookingId(payload);
        UUID userId = extractUserId(payload);
        
        notificationService.sendNotification(NotificationDtos.SendNotificationRequest.builder()
                .userId(userId)
                .bookingId(bookingId)
                .type(Notification.NotificationType.REFUND_COMPLETED)
                .channel(Notification.Channel.EMAIL)
                .subject("Refund Processed")
                .content("Your refund has been processed successfully.")
                .build());
    }
    
    private UUID extractUserId(String payload) {
        return UUID.randomUUID();
    }
    
    private UUID extractBookingId(String payload) {
        return UUID.randomUUID();
    }
}