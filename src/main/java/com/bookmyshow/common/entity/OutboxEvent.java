package com.bookmyshow.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_aggregate", columnList = "aggregate_id"),
        @Index(name = "idx_outbox_type", columnList = "event_type"),
        @Index(name = "idx_outbox_published", columnList = "published"),
        @Index(name = "idx_outbox_created", columnList = "created_at"),
        @Index(name = "idx_outbox_retry", columnList = "published, retry_count, next_retry_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent extends BaseEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "aggregate_id", nullable = false, columnDefinition = "UUID")
    private UUID aggregateId;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;
    
    @Column(name = "published", nullable = false)
    @Builder.Default
    private Boolean published = false;
    
    @Column(name = "published_at")
    private Instant publishedAt;
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;
    
    @Column(name = "last_error", length = 1000)
    private String lastError;
}