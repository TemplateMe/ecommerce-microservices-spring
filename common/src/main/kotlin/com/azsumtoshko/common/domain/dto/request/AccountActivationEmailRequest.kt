package com.azsumtoshko.common.domain.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AccountActivationEmailRequest(

    @field:NotBlank(message = "Recipient email must not be blank")
    @field:Email(message = "Recipient email must be a valid email address")
    val to: String,

    @field:NotBlank(message = "Username must not be blank")
    @field:Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Confirmation URL must not be blank")
    val confirmationUrl: String,

    @field:NotBlank(message = "Activation code must not be blank")
    @field:Size(min = 6, max = 100, message = "Activation code must be between 6 and 100 characters")
    val activationCode: String,

    @field:NotBlank(message = "Subject must not be blank")
    @field:Size(min = 3, max = 100, message = "Subject must be between 3 and 100 characters")
    val subject: String
)
