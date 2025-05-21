package com.azsumtoshko.common.domain.dto

data class PresignedUrlResponse(
    val url: String,
    val expiresAt: String
)