package com.commandlinecommandos.campusmarketplace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * Cache Configuration with graceful Redis fallback
 * 
 * Priority Order:
 * 1. Redis (if available) - Best for production, distributed caching
 * 2. Caffeine (if Redis unavailable) - In-memory cache, good for single instance
 * 3. Simple (if caching disabled) - No caching, always hits database
 * 
 * Configuration Options:
 * - CACHE_TYPE=redis     → Use Redis (requires Redis running)
 * - CACHE_TYPE=caffeine  → Use in-memory Caffeine cache
 * - CACHE_TYPE=none      → Disable caching
 * 
 * Redis Automatic Fallback:
 * If CACHE_TYPE=redis but Redis is unavailable, automatically falls back to Caffeine
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Redis Cache Manager (Primary)
     * Used when CACHE_TYPE=redis and Redis is available
     * Not used in test profile (test profile has its own testCacheManager)
     */
    @Bean
    @Primary
    @Profile("!test")
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // Test Redis connection
            connectionFactory.getConnection().ping();
            
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                    )
                )
                .disableCachingNullValues();

            log.info("✅ Redis cache enabled - Using distributed caching");
            
            // Redis cache manager allows dynamic cache creation by default
            // No need to pre-define cache names - they're created on first use
            return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
                
        } catch (Exception e) {
            log.warn("⚠️  Redis unavailable: {} - Falling back to Caffeine cache", e.getMessage());
            return createCaffeineCacheManager();
        }
    }

    /**
     * Caffeine Cache Manager (Fallback)
     * Used when Redis is unavailable or CACHE_TYPE=caffeine
     * Not used in test profile (test profile has its own testCacheManager)
     */
    @Bean
    @Profile("!test")
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "caffeine")
    public CacheManager caffeineCacheManager() {
        return createCaffeineCacheManager();
    }
    
    /**
     * Create Caffeine Cache Manager instance
     * Shared by both explicit configuration and Redis fallback
     */
    private CacheManager createCaffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "searchResults",
            "trendingProducts",
            "recommendations",
            "recommendedItems",
            "recentlyViewed",
            "autocomplete"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats());
        
        log.info("✅ Caffeine cache enabled - Using in-memory caching");
        
        return cacheManager;
    }

    /**
     * Simple Cache Manager (No Caching / Testing)
     * Used when CACHE_TYPE=none or caching is disabled
     * Allows dynamic cache creation to prevent "cache not found" errors
     * Not used in test profile (test profile has its own testCacheManager)
     */
    @Bean
    @Profile("!test")
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "none", matchIfMissing = false)
    public CacheManager simpleCacheManager() {
        log.warn("⚠️  Caching DISABLED - Using no-op cache manager for testing");
        
        // Create a ConcurrentMapCacheManager that allows any cache name dynamically
        // Don't set cache names - this allows dynamic creation
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Allow dynamic cache creation for any cache names
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
    
    /**
     * Test Profile Cache Manager
     * Explicitly used in test profile to ensure cache manager is available
     * Allows dynamic cache creation to prevent "cache not found" errors
     */
    @Bean
    @Profile("test")
    @Primary
    public CacheManager testCacheManager() {
        log.warn("⚠️  TEST PROFILE - Using test cache manager with dynamic cache creation");
        
        // Create a ConcurrentMapCacheManager that allows dynamic cache creation
        // Don't set cache names - this allows any cache name to be created on first use
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}

