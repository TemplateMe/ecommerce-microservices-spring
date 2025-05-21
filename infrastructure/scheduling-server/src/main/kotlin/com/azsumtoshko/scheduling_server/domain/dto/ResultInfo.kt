package com.azsumtoshko.scheduling_server.domain.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object ResultInfo {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    @Synchronized
    fun success(): String = success(null, null)

    @Synchronized
    fun success(obj: Any?): String = success(obj, null)

    @Synchronized
    fun success(obj: Any?, filterPropNames: Array<String>?): String = success(obj, true, filterPropNames)

    @Synchronized
    fun success(obj: Any?, isRefDetect: Boolean, filterPropNames: Array<String>?): String {
        val body = ResponseBodyInfo(0, "", obj)
        // You can implement property filtering here if needed
        return objectMapper.writeValueAsString(body)
    }

    @Synchronized
    fun error(code: Int, message: String): String = error(code, message, null)

    @Synchronized
    fun error(code: Int, message: String, obj: Any?): String {
        val body = ResponseBodyInfo(code, message, obj)
        return objectMapper.writeValueAsString(body)
    }
}