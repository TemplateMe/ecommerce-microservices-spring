package com.services.task_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskServiceApplication

fun main(args: Array<String>) {
	runApplication<TaskServiceApplication>(*args)
}
