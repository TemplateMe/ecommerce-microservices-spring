package com.azsumtoshko.notification_server.controller

import com.azsumtoshko.common.domain.dto.request.AccountActivationEmailRequest
import com.azsumtoshko.notification_server.service.SmtpService
import jakarta.validation.Valid
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
    fun sendAccountActivation(@RequestBody @Valid request: AccountActivationEmailRequest): String {
        smtpService.sendAccountVerificationEmail(request)
        return "Email sent successfully"
    }
}