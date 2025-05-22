package com.azsumtoshko.common.domain.dto.request.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class MessageReceivedEmailRequest(
    @field:Email
    @field:NotBlank
    val to: String,

    @field:NotBlank
    val subject: String,

    @field:NotBlank
    val senderName: String,

    @field:NotBlank
    val message: String
)