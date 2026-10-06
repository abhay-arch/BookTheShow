package com.bookmyshow.common.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RateLimiterConfiguration {
    
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig authConfig = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        
        RateLimiterConfig apiConfig = RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        
        RateLimiterConfig bookingConfig = RateLimiterConfig.custom()
                .limitForPeriod(30)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(10))
                .build();
        
        Map<String, RateLimiterConfig> configs = new HashMap<>();
        configs.put("auth", authConfig);
        configs.put("api", apiConfig);
        configs.put("booking", bookingConfig);
        
        return RateLimiterRegistry.of(configs);
    }
}