package com.azsumtoshko.common.domain.dto

data class ObjectDTO(
    val name: String,
    val size: Long,
    val lastModified: String,
    val contentType: String? = null
)