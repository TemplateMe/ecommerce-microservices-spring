package com.azsumtoshko.notification_server.controller

import com.azsumtoshko.common.domain.dto.request.email.*
import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.azsumtoshko.notification_server.service.SmtpService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notification/email")
class EmailController(
    private final val smtpService: SmtpService
) {
    @PostMapping("/account-activation")
    fun sendAccountActivation(@RequestBody @Valid request: AccountActivationEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendAccountActivationEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/forgot-password")
    fun sendForgotPassword(@RequestBody @Valid request: ForgotPasswordEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendForgotPasswordEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/feedback")
    fun sendFeedback(@RequestBody @Valid request: FeedbackEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendFeedbackEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/message-received")
    fun sendMessageReceived(@RequestBody @Valid request: MessageReceivedEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendMessageReceivedEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/order-received")
    fun sendOrderReceived(@RequestBody @Valid request: OrderReceivedEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendOrderReceivedEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/payment-received")
    fun sendPaymentReceived(@RequestBody @Valid request: PaymentReceivedEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendPaymentReceivedEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/verify-email")
    fun sendVerifyEmail(@RequestBody @Valid request: VerifyEmailRequest): ResponseEntity<ApiResponse> {
        val response = smtpService.sendVerifyEmail(request)
        return ResponseEntity.status(response.statusCode).body(response)
    }
}