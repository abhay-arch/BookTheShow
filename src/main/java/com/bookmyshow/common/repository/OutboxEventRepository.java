package com.bookmyshow.common.repository;

import com.bookmyshow.common.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    List<OutboxEvent> findByPublishedFalseAndRetryCountLessThanOrderByCreatedAtAsc(int maxRetries);
    
    @Query("SELECT o FROM OutboxEvent o WHERE o.published = false AND (o.nextRetryAt IS NULL OR o.nextRetryAt <= :now) AND o.retryCount < :maxRetries ORDER BY o.createdAt ASC")
    List<OutboxEvent> findReadyForRetry(@Param("now") Instant now, @Param("maxRetries") int maxRetries);
    
    List<OutboxEvent> findByAggregateId(UUID aggregateId);
}