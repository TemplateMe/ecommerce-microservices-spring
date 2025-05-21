package com.azsumtoshko.scheduling_server.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class HttpJobExecutor(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) {
    fun execute(url: String, method: String, headersJson: String?, body: String?) {
        val headers = try {
            objectMapper.readValue(headersJson, object : TypeReference<Map<String, String>>() {})
        } catch (e: Exception) {
            emptyMap()
        }

        webClient.method(HttpMethod.valueOf(method))
            .uri(url)
            .headers { it.setAll(headers) }
            .bodyValue(body ?: "")
            .retrieve()
            .toBodilessEntity()
            .block()
    }
}