package com.commandlinecommandos.campusmarketplace.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class RateLimitingConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("rateLimitBuckets");
    }

    @Bean
    public ConcurrentHashMap<String, RateLimitData> rateLimitBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Simple rate limiter data structure
     */
    public static class RateLimitData {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());
        private final int maxRequests;
        private final long windowSizeMs;

        public RateLimitData(int maxRequests, long windowSizeMs) {
            this.maxRequests = maxRequests;
            this.windowSizeMs = windowSizeMs;
        }

        public boolean tryConsume() {
            long now = System.currentTimeMillis();
            long windowStartTime = windowStart.get();
            
            // Reset window if it has expired
            if (now - windowStartTime > windowSizeMs) {
                if (windowStart.compareAndSet(windowStartTime, now)) {
                    requestCount.set(0);
                }
            }
            
            // Check if we can make a request
            int currentCount = requestCount.get();
            if (currentCount >= maxRequests) {
                return false;
            }
            
            // Increment and check again (race condition protection)
            int newCount = requestCount.incrementAndGet();
            return newCount <= maxRequests;
        }
    }

    /**
     * Creates a rate limiter for authentication endpoints
     * 5 requests per minute per IP address
     */
    public static RateLimitData createAuthRateLimiter() {
        return new RateLimitData(5, 60_000); // 5 requests per 60 seconds
    }

    /**
     * Creates a rate limiter for general API endpoints
     * 100 requests per minute per IP address
     */
    public static RateLimitData createGeneralRateLimiter() {
        return new RateLimitData(100, 60_000); // 100 requests per 60 seconds
    }
}
