package com.bookmyshow.common.kafka;

import com.bookmyshow.common.entity.OutboxEvent;
import com.bookmyshow.common.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${bookmyshow.kafka.outbox.batch-size:100}")
    private int batchSize;
    
    @Value("${bookmyshow.kafka.outbox.max-retries:5}")
    private int maxRetries;
    
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByPublishedFalseAndRetryCountLessThanOrderByCreatedAtAsc(maxRetries);
        
        for (OutboxEvent event : events) {
            try {
                String topic = determineTopic(event.getEventType());
                String key = event.getAggregateId().toString();
                
                kafkaTemplate.send(topic, key, event.getPayload()).get();
                
                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
                
                log.debug("Published outbox event: {} to topic: {}", event.getId(), topic);
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(e.getMessage());
                event.setNextRetryAt(Instant.now().plusSeconds(30 * event.getRetryCount()));
                outboxEventRepository.save(event);
                
                log.warn("Failed to publish outbox event: {}, retry: {}", event.getId(), event.getRetryCount(), e);
            }
        }
    }
    
    private String determineTopic(String eventType) {
        return switch (eventType) {
            case "BookingCreated", "BookingConfirmed", "BookingCancelled", "SeatHoldCreated", "SeatHoldExpired" -> "booking-events";
            case "PaymentSucceeded", "PaymentFailed", "PaymentRefunded", "RefundInitiated" -> "payment-events";
            case "MovieCreated", "MovieUpdated", "VenueCreated", "ScreenCreated" -> "catalog-events";
            case "NotificationRequested" -> "notification-events";
            default -> "booking-events";
        };
    }
}