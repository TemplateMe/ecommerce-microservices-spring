package com.azsumtoshko.common.domain.dto.request.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class VerifyEmailRequest(
    @field:Email
    @field:NotBlank
    val to: String,

    @field:NotBlank
    val subject: String,

    @field:NotBlank
    val verificationUrl: String,

    @field:NotBlank
    val username: String
)