package com.azsumtoshko.common.domain.dto.response.base

data class ApiResponse(
    val success: Boolean,
    val statusCode: Int = 200,
    val message: String? = null,
    val data: Any? = null,
    val errors: List<String>? = null
) {

}
