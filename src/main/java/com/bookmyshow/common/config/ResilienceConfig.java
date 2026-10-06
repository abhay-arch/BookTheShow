package com.bookmyshow.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ResilienceConfig {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
        
        CircuitBreakerConfig paymentConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(30)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
        
        Map<String, CircuitBreakerConfig> configs = new HashMap<>();
        configs.put("default", defaultConfig);
        configs.put("payment", paymentConfig);
        
        return CircuitBreakerRegistry.of(configs);
    }
    
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig defaultConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                .build();
        
        RetryConfig paymentConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                .build();
        
        Map<String, RetryConfig> configs = new HashMap<>();
        configs.put("default", defaultConfig);
        configs.put("payment", paymentConfig);
        
        return RetryRegistry.of(configs);
    }
    
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .build();
        
        TimeLimiterConfig paymentConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .build();
        
        Map<String, TimeLimiterConfig> configs = new HashMap<>();
        configs.put("default", defaultConfig);
        configs.put("payment", paymentConfig);
        
        return TimeLimiterRegistry.of(configs);
    }
}