package com.services.task_service.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

/**
 * Cache configuration for Spring Cloud LoadBalancer
 * Configures Caffeine cache to replace the default cache implementation
 */
@Configuration
@EnableCaching
class CacheConfiguration {

    /**
     * Configures Caffeine cache manager for optimal performance
     * 
     * @return CacheManager configured with Caffeine cache
     */
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(1000) // Maximum number of entries
                .expireAfterWrite(10, TimeUnit.MINUTES) // Cache expiration
                .recordStats() // Enable cache statistics
        )
        return cacheManager
    }
} 