package com.azsumtoshko.common.domain.dto.request.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class PaymentReceivedEmailRequest(
    @field:Email
    @field:NotBlank
    val to: String,

    @field:NotBlank
    val subject: String,

    @field:NotBlank
    val paymentAmount: String,

    @field:NotBlank
    val paymentDate: String,

    @field:NotBlank
    val username: String
)