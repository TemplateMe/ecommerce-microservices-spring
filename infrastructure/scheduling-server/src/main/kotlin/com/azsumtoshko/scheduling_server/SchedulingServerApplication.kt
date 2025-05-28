package com.azsumtoshko.scheduling_server

import com.azsumtoshko.scheduling_server.job.HttpJob
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SchedulingServerApplication

fun main(args: Array<String>) {
	val context: ApplicationContext = runApplication<SchedulingServerApplication>(*args)
	HttpJob.context = context
}
