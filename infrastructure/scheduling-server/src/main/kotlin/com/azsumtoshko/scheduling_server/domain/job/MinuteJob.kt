package com.azsumtoshko.scheduling_server.domain.job

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException

class MinuteJob : Job {
    private val logger: Logger = LogManager.getLogger(javaClass)

    @Throws(JobExecutionException::class)
    override fun execute(context: JobExecutionContext) {
        logger.info("JobName: {}", context.jobDetail.key.name)
    }
}