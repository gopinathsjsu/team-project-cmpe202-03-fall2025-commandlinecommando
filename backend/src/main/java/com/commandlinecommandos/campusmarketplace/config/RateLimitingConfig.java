package com.commandlinecommandos.campusmarketplace.config;

import com.github.bucket4j.Bandwidth;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.Refill;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("rateLimitBuckets");
    }

    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Creates a rate limiter bucket for authentication endpoints
     * 5 requests per minute per IP address
     */
    public static Bucket createAuthRateLimiter() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * Creates a rate limiter bucket for general API endpoints
     * 100 requests per minute per IP address
     */
    public static Bucket createGeneralRateLimiter() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }
}
