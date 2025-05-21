package com.azsumtoshko.file_server.service

import com.azsumtoshko.common.domain.dto.BucketDTO
import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.minio.GetBucketNotificationArgs
import io.minio.GetBucketPolicyArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.RemoveBucketArgs
import io.minio.SetBucketNotificationArgs
import io.minio.messages.NotificationConfiguration
import org.springframework.stereotype.Service

@Service
class BucketService(
    private val minioClient: MinioClient
) {
    fun bucketExists(name: String): Boolean = minioClient.bucketExists(
        io.minio.BucketExistsArgs.builder()
            .bucket(name)
            .build()
    )

    fun listBuckets(): ApiResponse {
        try {
            val buckets: List<BucketDTO> = minioClient.listBuckets().map { BucketDTO(
                name = it.name(),
                createdAt = it.creationDate().toString()
            ) }

            return ApiResponse(
                success = true,
                data = buckets,
                statusCode = 200,
                message = "Buckets listed successfully."
            )
        }catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error listing buckets",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun createBucket(name: String): ApiResponse {
        try {
            if (this.bucketExists(name)) {
                return ApiResponse(
                    success = false,
                    statusCode = 400,
                    message = "Bucket $name already exists."
                )
            }

            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(name)
                    .build()
            )
            return ApiResponse(
                success = true,
                statusCode = 201,
                message = "Bucket $name created successfully."
            )

        }catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error creating bucket $name",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun removeBucket(name: String): ApiResponse {
        try{
            if(!this.bucketExists(name)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $name does not exist."
                )
            }

            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(name).build())

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Bucket $name removed successfully."
            )

        }catch (e: Exception) {
            e.printStackTrace()

            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error removing bucket $name",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun getBucketPolicy(name: String): ApiResponse {
        try{
            if(!this.bucketExists(name)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $name does not exist."
                )
            }

            val policy = minioClient.getBucketPolicy(
                GetBucketPolicyArgs.builder()
                    .bucket(name)
                    .build()
            )

            val objectMapper = jacksonObjectMapper()
            val parsedPolicy: Map<String, Any> = objectMapper.readValue(policy)

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = parsedPolicy,
                message = "Bucket policy retrieved successfully."
            )
        }catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error getting bucket policy",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun setBucketPolicy(name: String, policy: String): ApiResponse {
        try{
            if(!this.bucketExists(name)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $name does not exist."
                )
            }

            minioClient.setBucketPolicy(
                io.minio.SetBucketPolicyArgs.builder()
                    .bucket(name)
                    .config(policy)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Bucket policy set successfully."
            )
        }catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 400,
                message = "Error setting bucket policy",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun setBucketNotification(bucketName: String, config: NotificationConfiguration): ApiResponse {
        try {
            if (!bucketExists(bucketName))
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $bucketName not found"
                )

            minioClient.setBucketNotification(
                SetBucketNotificationArgs
                    .builder()
                    .bucket(bucketName)
                    .config(config)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Notification set"
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error setting notification: ${e.message}"
            )
        }
    }

    fun getBucketNotification(bucketName: String): ApiResponse {
        try {
            if (!bucketExists(bucketName))
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $bucketName not found"
                )

            val config = minioClient.getBucketNotification(
                GetBucketNotificationArgs
                    .builder()
                    .bucket(bucketName)
                    .build()
            )

            val queueConfigs = config.queueConfigurationList().map { qc ->
                mapOf(
                    "id" to qc.id(),
                    "queue" to qc.queue(),
                    "events" to qc.events(),
                    "filter" to qc.filterRuleList().map { fr ->
                        mapOf("name" to fr.name(), "value" to fr.value())
                    }
                )
            }

            val topicConfigs = config.topicConfigurationList().map { tc ->
                mapOf(
                    "id" to tc.id(),
                    "topic" to tc.topic(),
                    "events" to tc.events(),
                    "filter" to tc.filterRuleList().map { fr ->
                        mapOf("name" to fr.name(), "value" to fr.value())
                    }
                )
            }

            val cloudFunctionConfigs = config.cloudFunctionConfigurationList().map { cc ->
                mapOf(
                    "id" to cc.id(),
                    "cloudFunction" to cc.cloudFunction(),
                    "events" to cc.events(),
                    "filter" to cc.filterRuleList().map { fr ->
                        mapOf("name" to fr.name(), "value" to fr.value())
                    }
                )
            }

            val serializableConfig = mapOf(
                "queueConfigurations" to queueConfigs,
                "topicConfigurations" to topicConfigs,
                "cloudFunctionConfigurations" to cloudFunctionConfigs
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = serializableConfig,
                message = "Notification retrieved"
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error getting notification: ${e.message}"
            )
        }
    }
}