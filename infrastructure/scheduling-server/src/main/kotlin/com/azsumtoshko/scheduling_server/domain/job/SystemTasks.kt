package com.azsumtoshko.scheduling_server.domain.job

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

// 1. Predefined Tasks (Always Running)
@Component
class SystemTasks {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 * * * *") // Every hour
    fun healthCheck() {
        logger.info("System health check completed")
        // Add actual health check logic
    }

    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    fun databaseCleanup() {
        logger.info("Performing database maintenance")
        // Add cleanup logic
    }
}