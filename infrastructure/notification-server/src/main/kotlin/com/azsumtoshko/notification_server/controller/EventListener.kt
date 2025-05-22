package com.azsumtoshko.notification_server.controller

import com.azsumtoshko.common.constant.*
import com.azsumtoshko.common.domain.dto.request.email.*
import com.azsumtoshko.notification_server.service.SmtpService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class EventListener(
    private val smtpService: SmtpService
) {
    @KafkaListener(topics = [TOPIC_ACCOUNT_ACTIVATION], groupId = NOTIFICATION_GROUP_ID)
    fun handleAccountActivation(event: AccountActivationEmailRequest) {
        smtpService.sendAccountActivationEmail(event)
    }

    @KafkaListener(topics = [TOPIC_FORGOT_PASSWORD], groupId = NOTIFICATION_GROUP_ID)
    fun handleForgotPassword(event: ForgotPasswordEmailRequest) {
        smtpService.sendForgotPasswordEmail(event)
    }

    @KafkaListener(topics = [TOPIC_FEEDBACK], groupId = NOTIFICATION_GROUP_ID)
    fun handleFeedback(event: FeedbackEmailRequest) {
        smtpService.sendFeedbackEmail(event)
    }

    @KafkaListener(topics = [TOPIC_MESSAGE_RECEIVED], groupId = NOTIFICATION_GROUP_ID)
    fun handleMessageReceived(event: MessageReceivedEmailRequest) {
        smtpService.sendMessageReceivedEmail(event)
    }

    @KafkaListener(topics = [TOPIC_ORDER_RECEIVED], groupId = NOTIFICATION_GROUP_ID)
    fun handleOrderReceived(event: OrderReceivedEmailRequest) {
        smtpService.sendOrderReceivedEmail(event)
    }

    @KafkaListener(topics = [TOPIC_PAYMENT_RECEIVED], groupId = NOTIFICATION_GROUP_ID)
    fun handlePaymentReceived(event: PaymentReceivedEmailRequest) {
        smtpService.sendPaymentReceivedEmail(event)
    }

    @KafkaListener(topics = [TOPIC_VERIFY_EMAIL], groupId = NOTIFICATION_GROUP_ID)
    fun handleVerifyEmail(event: VerifyEmailRequest) {
        smtpService.sendVerifyEmail(event)
    }
}