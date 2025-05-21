package com.azsumtoshko.notification_server.controller

import com.azsumtoshko.common.constant.TOPIC_ACCOUNT_ACTIVATION
import com.azsumtoshko.common.domain.dto.request.AccountActivationEmailRequest
import com.azsumtoshko.notification_server.service.SmtpService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class EventListener(
    private val smtpService: SmtpService,
    service: SmtpService
) {
    @KafkaListener(
        topics = [TOPIC_ACCOUNT_ACTIVATION], // Topic to listen to
        groupId = "notification-service-group" // Consumer group
    )
    fun handleEmailNotification(event: AccountActivationEmailRequest) {
        smtpService.sendAccountVerificationEmail(event)
    }
}