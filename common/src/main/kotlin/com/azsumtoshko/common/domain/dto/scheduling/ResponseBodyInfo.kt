package com.azsumtoshko.common.domain.dto.scheduling

data class ResponseBodyInfo<T>(
    var errorCode: Int = 0,
    var errorText: String? = null,
    var data: T? = null
)
