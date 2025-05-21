package com.azsumtoshko.file_server.service

import com.azsumtoshko.common.domain.dto.ObjectDTO
import com.azsumtoshko.common.domain.dto.PresignedUrlResponse
import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import io.minio.CopyObjectArgs
import io.minio.CopySource
import io.minio.DeleteObjectTagsArgs
import io.minio.Directive
import io.minio.GetObjectArgs
import io.minio.GetObjectRetentionArgs
import io.minio.messages.Retention
import io.minio.messages.RetentionMode
import io.minio.GetObjectTagsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.SetObjectRetentionArgs
import io.minio.SetObjectTagsArgs
import io.minio.StatObjectArgs
import io.minio.http.Method
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class ObjectService(
    private val minioClient: MinioClient
) {
    fun bucketExists(name: String): Boolean = minioClient.bucketExists(
        io.minio.BucketExistsArgs.builder()
            .bucket(name)
            .build()
    )

    fun objectExists(bucketName: String, objectName: String): Boolean =
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
            true
        } catch (e: Exception) {
            false
        }

    fun uploadObject(
        bucketName: String,
        file: MultipartFile,
        objectName: String? = null
    ): ApiResponse {
        try {
            if (!bucketExists(bucketName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $bucketName not found"
                )
            }

            val finalObjectName = objectName ?: UUID.randomUUID().toString()

            if (objectExists(bucketName, finalObjectName)) {
                // This conflict will only happen when a name is provided.
                // It's impossible to generate random id which to be used since UUID by itself uses a timestamp from the current time of generation
                return ApiResponse(
                    success = false,
                    statusCode = 409,
                    message = "Object '$finalObjectName' already exists in bucket '$bucketName'."
                )
            }

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(finalObjectName)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(file.contentType)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 201,
                data = finalObjectName,
                message = "Object uploaded successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error uploading object",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun listObjects(bucketName: String): ApiResponse {
        try {
            if (!bucketExists(bucketName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $bucketName not found"
                )
            }

            val objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build()
            ).map {
                val obj = it.get()
                // Fetch metadata using statObject
                val stat = minioClient.statObject(
                    StatObjectArgs.builder()
                        .bucket(bucketName)
                        .`object`(obj.objectName())
                        .build()
                )
                ObjectDTO(
                    name = obj.objectName(),
                    size = obj.size(),
                    lastModified = obj.lastModified().toString(),
                    contentType = stat.contentType()
                )
            }.toList()

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = objects,
                message = "Objects listed successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error listing objects",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun getObject(bucketName: String, objectName: String): ApiResponse {
        try {
            if (!bucketExists(bucketName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Bucket $bucketName not found"
                )
            }

            val stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            val bytes = stream.readAllBytes()
            val resource = ByteArrayResource(bytes)

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = resource,
                message = "Object retrieved successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 404,
                message = "Object $objectName not found",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun deleteObject(bucketName: String, objectName: String): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Object deleted successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error deleting object",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun generatePresignedUrl(
        bucketName: String,
        objectName: String,
        method: Method,
        expiry: Int = 7
    ): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            val url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(method)
                    .bucket(bucketName)
                    .`object`(objectName)
                    .expiry(expiry, TimeUnit.DAYS)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = PresignedUrlResponse(
                    url = url,
                    expiresAt = LocalDateTime.now().plusDays(expiry.toLong()).toString()
                ),
                message = "Presigned URL generated successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error generating presigned URL",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun updateObjectMetadata(
        bucketName: String,
        objectName: String,
        metadata: Map<String, String>
    ): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            val stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            val contentType = metadata["Content-Type"] ?: stat.contentType() ?: "application/octet-stream"

            val userMetadata = metadata.toMutableMap()
            userMetadata["Content-Type"] = contentType

            // Copy object to itself with new metadata
            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .source(
                        CopySource.builder()
                            .bucket(bucketName)
                            .`object`(objectName)
                            .build()
                    )
                    .userMetadata(userMetadata)
                    .metadataDirective(Directive.REPLACE)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Object metadata updated successfully"
            )
        } catch (e: Exception) {
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error updating metadata: ${e.message}",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun copyObject(
        destBucket: String,
        destObject: String,
        sourceBucket: String,
        sourceObject: String
    ): ApiResponse {
        try {
            if (!bucketExists(sourceBucket) || !bucketExists(destBucket)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Source or destination bucket not found"
                )
            }

            minioClient.copyObject(
                CopyObjectArgs.builder()
                    .bucket(destBucket)
                    .`object`(destObject)
                    .source(
                        CopySource.builder()
                            .bucket(sourceBucket)
                            .`object`(sourceObject)
                            .build()
                    )
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Object copied successfully"
            )
        } catch (e: Exception) {
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error copying object: ${e.message}",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun getObjectMetadata(
        bucketName: String,
        objectName: String
    ): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            val stat = minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = mapOf(
                    "metadata" to stat.userMetadata(),
                    "contentType" to stat.contentType(),
                    "size" to stat.size(),
                    "lastModified" to stat.lastModified()
                ),
                message = "Metadata retrieved successfully"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error retrieving metadata: ${e.message}",
                errors = listOf(e.message ?: "Unknown error")
            )
        }
    }

    fun setObjectTags(bucketName: String, objectName: String, tags: Map<String, String>): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            minioClient.setObjectTags(
                SetObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .tags(tags)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Tags set"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error setting tags: ${e.message}"
            )
        }
    }

    fun getObjectTags(bucketName: String, objectName: String): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            val tags = minioClient.getObjectTags(
                GetObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = tags.get() ?: emptyMap<String, String>(),
                message = "Tags retrieved"
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error getting tags: ${e.message}"
            )
        }
    }

    fun deleteObjectTags(bucketName: String, objectName: String): ApiResponse {
        try {
            if (!objectExists(bucketName, objectName)) {
                return ApiResponse(
                    success = false,
                    statusCode = 404,
                    message = "Object $objectName not found in bucket $bucketName"
                )
            }

            minioClient.deleteObjectTags(
                DeleteObjectTagsArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Tags deleted"
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error deleting tags: ${e.message}"
            )
        }
    }

    fun setObjectRetention(bucketName: String, objectName: String, mode: String, retainUntil: ZonedDateTime): ApiResponse {
        try {
            val retentionMode = when (mode.uppercase()) {
                "GOVERNANCE" -> RetentionMode.GOVERNANCE
                "COMPLIANCE" -> RetentionMode.COMPLIANCE
                else -> throw IllegalArgumentException("Invalid retention mode: $mode")
            }
            val retention = Retention(retentionMode, retainUntil)

            minioClient.setObjectRetention(
                SetObjectRetentionArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .config(retention)
                    .build()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                message = "Retention set"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error setting retention: ${e.message}"
            )
        }
    }

    fun getObjectRetention(bucketName: String, objectName: String): ApiResponse {
        try {
            val retention = minioClient.getObjectRetention(
                GetObjectRetentionArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )

            val retentionInfo = mapOf(
                "mode" to retention.mode()?.name,
                "retainUntilDate" to retention.retainUntilDate()?.toString()
            )

            return ApiResponse(
                success = true,
                statusCode = 200,
                data = retentionInfo,
                message = "Retention retrieved"
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ApiResponse(
                success = false,
                statusCode = 500,
                message = "Error getting retention: ${e.message}"
            )
        }
    }
}
