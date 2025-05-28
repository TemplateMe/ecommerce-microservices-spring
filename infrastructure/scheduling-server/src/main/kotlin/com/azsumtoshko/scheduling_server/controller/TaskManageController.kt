package com.azsumtoshko.scheduling_server.controller

import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.azsumtoshko.common.domain.entity.TaskInfo
import com.azsumtoshko.scheduling_server.service.TaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/scheduling")
class TaskManageController (
    private val taskServiceImpl: TaskService
) {
    @GetMapping("list")
    fun list(): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.list()

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PostMapping("create")
    fun createTask(@ModelAttribute info: TaskInfo): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.addJob(info)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PutMapping("edit")
    fun editTask(@ModelAttribute info: TaskInfo): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.edit(info)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @DeleteMapping("delete/{id}")
    fun delete(@PathVariable id: Int): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.delete(id)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PatchMapping("pause/{id}")
    fun pause(@PathVariable id: Int): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.pause(id)

        return ResponseEntity.status(response.statusCode).body(response)
    }

    @PatchMapping("resume/{id}")
    fun resume(@PathVariable id: Int): ResponseEntity<ApiResponse> {
        val response = taskServiceImpl.resume(id)

        return ResponseEntity.status(response.statusCode).body(response)
    }
}