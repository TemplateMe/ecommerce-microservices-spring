package com.azsumtoshko.file_server.controller

import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.azsumtoshko.file_server.service.BucketService
import io.minio.messages.NotificationConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bucket")
class BucketController(
    private final val bucketService: BucketService
) {

    @PostMapping
    fun createBucket(@RequestParam name: String): ResponseEntity<ApiResponse> {
        val response: ApiResponse = bucketService.createBucket(name)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping
    fun listBuckets(): ResponseEntity<ApiResponse> {
        val response = bucketService.listBuckets()

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @DeleteMapping("/{bucketName}")
    fun deleteBucket(@PathVariable bucketName: String): ResponseEntity<ApiResponse> {
        val response = bucketService.removeBucket(bucketName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{bucketName}/policy")
    fun getBucketPolicy(@PathVariable bucketName: String): ResponseEntity<ApiResponse> {
        val response = bucketService.getBucketPolicy(bucketName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{bucketName}/policy")
    fun setBucketPolicy(
        @PathVariable bucketName: String,
        @RequestBody policyJson: String
    ): ResponseEntity<ApiResponse> {
        val response = bucketService.setBucketPolicy(bucketName, policyJson)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("/{bucketName}/notifications")
    fun setNotification(@PathVariable bucketName: String, @RequestBody config: NotificationConfiguration): ResponseEntity<ApiResponse> {
        val response = bucketService.setBucketNotification(bucketName, config)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @GetMapping("/{bucketName}/notifications")
    fun getNotification(@PathVariable bucketName: String): ResponseEntity<ApiResponse> {
        val response = bucketService.getBucketNotification(bucketName)

        return ResponseEntity.status(response.statusCode).body(response)
    }

}
