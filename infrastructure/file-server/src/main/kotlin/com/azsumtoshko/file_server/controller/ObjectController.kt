package com.azsumtoshko.file_server.controller

import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.azsumtoshko.file_server.service.ObjectService
import io.minio.http.Method
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime

@RestController
@RequestMapping("/api/v1/bucket/{bucketName}/objects")
class ObjectController(
    private val objectService: ObjectService
) {

    @PostMapping
    fun uploadObject(
        @PathVariable bucketName: String,
        @RequestPart file: MultipartFile,
        @RequestParam(required = false) objectName: String?
    ): ResponseEntity<ApiResponse> {
        val response = objectService.uploadObject(bucketName, file, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping
    fun listObjects(
        @PathVariable bucketName: String
    ): ResponseEntity<ApiResponse> {
        val response = objectService.listObjects(bucketName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{objectName}")
    fun getObject(
        @PathVariable bucketName: String,
        @PathVariable objectName: String
    ): ResponseEntity<Any> {
        val response = objectService.getObject(bucketName, objectName)

        return if (response.statusCode == 200)
            ResponseEntity.status(response.statusCode).body(response.data)
        else
            ResponseEntity.status(response.statusCode).body(response)
    }

    @DeleteMapping("/{objectName}")
    fun deleteObject(
        @PathVariable bucketName: String,
        @PathVariable objectName: String
    ): ResponseEntity<ApiResponse> {
        val response = objectService.deleteObject(bucketName, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{objectName}/url")
    fun generateDownloadUrl(
        @PathVariable bucketName: String,
        @PathVariable objectName: String,
        @RequestParam(defaultValue = "7") expiryDays: Int
    ): ResponseEntity<ApiResponse> {
        val response = objectService.generatePresignedUrl(
            bucketName,
            objectName,
            Method.GET,
            expiryDays
        )

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/{objectName}/url")
    fun generateUploadUrl(
        @PathVariable bucketName: String,
        @PathVariable objectName: String,
        @RequestParam(defaultValue = "7") expiryDays: Int
    ): ResponseEntity<ApiResponse> {
        val response = objectService.generatePresignedUrl(
            bucketName,
            objectName,
            Method.PUT,
            expiryDays
        )

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{objectName}")
    fun updateObjectMetadata(
        @PathVariable bucketName: String,
        @PathVariable objectName: String,
        @RequestBody metadata: Map<String, String>
    ): ResponseEntity<ApiResponse> {
        val response = objectService.updateObjectMetadata(bucketName, objectName, metadata)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("/{objectName}/copy")
    fun copyObject(
        @PathVariable bucketName: String,
        @PathVariable objectName: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<ApiResponse> {
        val sourceBucket = request["sourceBucket"] ?: ""
        val sourceObject = request["sourceObject"] ?: ""

        val response = objectService.copyObject(
            bucketName,
            objectName,
            sourceBucket,
            sourceObject
        )

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{objectName}/metadata")
    fun getObjectMetadata(
        @PathVariable bucketName: String,
        @PathVariable objectName: String
    ): ResponseEntity<ApiResponse> {
        val response = objectService.getObjectMetadata(bucketName, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{objectName}/tags")
    fun setTags(@PathVariable bucketName: String, @PathVariable objectName: String, @RequestBody tags: Map<String, String>): ResponseEntity<ApiResponse> {
        val response = objectService.setObjectTags(bucketName, objectName, tags)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{objectName}/tags")
    fun getTags(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<ApiResponse> {
        val response = objectService.getObjectTags(bucketName, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @DeleteMapping("/{objectName}/tags")
    fun deleteTags(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<ApiResponse> {
        val response = objectService.deleteObjectTags(bucketName, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{objectName}/retention")
    fun setRetention(
        @PathVariable bucketName: String,
        @PathVariable objectName: String,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<ApiResponse> {
        val mode = body["mode"] ?: "GOVERNANCE"
        val retainUntil = ZonedDateTime.parse(body["retainUntilDate"]!!)

        val response = objectService.setObjectRetention(bucketName, objectName, mode, retainUntil)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{objectName}/retention")
    fun getRetention(@PathVariable bucketName: String, @PathVariable objectName: String): ResponseEntity<ApiResponse> {
        val response = objectService.getObjectRetention(bucketName, objectName)

        return ResponseEntity.status(response.statusCode).body(response)
    }
}