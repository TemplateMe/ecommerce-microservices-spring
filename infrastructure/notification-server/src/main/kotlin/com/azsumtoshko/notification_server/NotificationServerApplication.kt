package com.azsumtoshko.notification_server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotificationServerApplication

fun main(args: Array<String>) {
	runApplication<NotificationServerApplication>(*args)
}
