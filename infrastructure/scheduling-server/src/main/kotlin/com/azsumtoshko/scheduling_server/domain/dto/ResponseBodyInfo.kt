package com.azsumtoshko.scheduling_server.domain.dto

data class ResponseBodyInfo<T>(
    var errorCode: Int = 0,
    var errorText: String? = null,
    var data: T? = null
)
