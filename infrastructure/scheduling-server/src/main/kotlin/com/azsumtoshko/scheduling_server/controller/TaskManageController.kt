package com.azsumtoshko.scheduling_server.controller

import com.azsumtoshko.scheduling_server.domain.dto.ResultInfo
import com.azsumtoshko.scheduling_server.domain.entity.TaskInfo
import com.azsumtoshko.scheduling_server.service.TaskService
import com.azsumtoshko.scheduling_server.util.exception.ServiceException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class TaskManageController @Autowired constructor(
    private val taskServiceImpl: TaskService
) {
    private val objectMapper = jacksonObjectMapper()

    @GetMapping("list", produces = ["application/json; charset=UTF-8"])
    fun list(): String {
        val infos = taskServiceImpl.list()
        val map = mapOf(
            "rows" to infos,
            "total" to infos.size
        )
        return objectMapper.writeValueAsString(map)
    }

    @PostMapping("save", produces = ["application/json; charset=UTF-8"])
    fun save(@ModelAttribute info: TaskInfo): String {
        return try {
            if (info.id == 0) {
                taskServiceImpl.addJob(info)
            } else {
                taskServiceImpl.edit(info)
            }
            ResultInfo.success()
        } catch (e: ServiceException) {
            ResultInfo.error(-1, e.message ?: "Unknown error")
        }
    }

    @DeleteMapping("delete/{jobName}/{jobGroup}", produces = ["application/json; charset=UTF-8"])
    fun delete(@PathVariable jobName: String, @PathVariable jobGroup: String): String {
        return try {
            taskServiceImpl.delete(jobName, jobGroup)
            ResultInfo.success()
        } catch (e: ServiceException) {
            ResultInfo.error(-1, e.message ?: "Unknown error")
        }
    }

    @PatchMapping("pause/{jobName}/{jobGroup}", produces = ["application/json; charset=UTF-8"])
    fun pause(@PathVariable jobName: String, @PathVariable jobGroup: String): String {
        return try {
            taskServiceImpl.pause(jobName, jobGroup)
            ResultInfo.success()
        } catch (e: ServiceException) {
            ResultInfo.error(-1, e.message ?: "Unknown error")
        }
    }

    @PatchMapping("resume/{jobName}/{jobGroup}", produces = ["application/json; charset=UTF-8"])
    fun resume(@PathVariable jobName: String, @PathVariable jobGroup: String): String {
        return try {
            taskServiceImpl.resume(jobName, jobGroup)
            ResultInfo.success()
        } catch (e: ServiceException) {
            ResultInfo.error(-1, e.message ?: "Unknown error")
        }
    }
}