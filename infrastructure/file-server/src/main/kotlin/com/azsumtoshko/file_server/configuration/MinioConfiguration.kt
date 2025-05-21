package com.azsumtoshko.file_server.configuration

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfiguration(
    @Value("\${minio.url}") private val url: String,
    @Value("\${minio.access-key}") private val accessKey: String,
    @Value("\${minio.secret-key}") private val secretKey: String
) {
    @Bean
    fun minioClient(): MinioClient =
        MinioClient.builder()
            .endpoint(url)
            .credentials(accessKey, secretKey)
            .build()
}
