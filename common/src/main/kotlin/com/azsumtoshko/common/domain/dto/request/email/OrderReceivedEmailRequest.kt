package com.azsumtoshko.common.domain.dto.request.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class OrderReceivedEmailRequest(
    @field:Email
    @field:NotBlank
    val to: String,

    @field:NotBlank
    val subject: String,

    @field:NotBlank
    val orderId: String,

    @field:NotBlank
    val orderDate: String,

    @field:NotBlank
    val username: String
)