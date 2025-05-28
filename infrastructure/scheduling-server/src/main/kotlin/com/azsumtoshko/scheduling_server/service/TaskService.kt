package com.azsumtoshko.scheduling_server.service

import com.azsumtoshko.common.constant.JOB_CLASS_PACKAGE_PREFIX
import com.azsumtoshko.common.constant.TASK_ID_JOB_DATA_KEY
import com.azsumtoshko.common.domain.dto.response.base.ApiResponse
import com.azsumtoshko.common.domain.entity.TaskInfo
import com.azsumtoshko.scheduling_server.util.exception.ServiceException
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.HashSet

@Service
class TaskService (
    private val scheduler: Scheduler
) {
    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    private fun getCurrentDateTime(): String = DATE_TIME_FORMATTER.format(LocalDateTime.now())

    private fun createJobKey(jobName: String, jobGroup: String): JobKey = JobKey.jobKey(jobName, jobGroup)

    private fun createTriggerKey(jobName: String, jobGroup: String): TriggerKey = TriggerKey.triggerKey(jobName, jobGroup)

    private fun createCronTrigger(triggerKey: TriggerKey, cronExpression: String, createTime: String): Trigger {
        val schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
            .withMisfireHandlingInstructionDoNothing()
        return TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withDescription(createTime)
            .withSchedule(schedBuilder)
            .build()
    }

    private fun getJobStatus(trigger: Trigger): String {
        return try {
            scheduler.getTriggerState(trigger.key).name
        } catch (e: SchedulerException) {
            "ERROR"
        }
    }

    fun list(): ApiResponse {
        val list = mutableListOf<TaskInfo>()
        try {
            for (groupJob in scheduler.jobGroupNames) {
                for (jobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupJob))) {
                    val triggers = scheduler.getTriggersOfJob(jobKey)
                    for (trigger in triggers) {
                        val jobDetail = scheduler.getJobDetail(jobKey)
                        val info = TaskInfo(
                            id = jobDetail.jobDataMap[TASK_ID_JOB_DATA_KEY] as? Int ?: 0,
                            jobName = jobKey.name,
                            jobGroup = jobKey.group,
                            jobDescription = jobDetail.description,
                            jobStatus = getJobStatus(trigger),
                            cronExpression = if (trigger is CronTrigger) trigger.cronExpression else "",
                            createTime = if (trigger is CronTrigger) trigger.description else ""
                        )
                        list.add(info)
                    }
                }
            }
            return ApiResponse(
                success = true,
                data = list,
                statusCode = 200,
                message = "Tasks listed successfully."
            )
        } catch (e: SchedulerException) {
            return handleGenericException(e, "Failed to list tasks")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addJob(info: TaskInfo): ApiResponse {
        validateTaskInfoInputs(info)
        val simpleJobName = info.jobName!!
        val jobGroup = info.jobGroup!!
        val cronExpression = info.cronExpression!!
        val jobDescription = info.jobDescription
        val timestamp = System.currentTimeMillis()
        val random = kotlin.random.Random.nextInt(1000)
        val taskId = (timestamp % Int.MAX_VALUE).toInt() + random
        info.id = taskId

        val fullJobClassName = JOB_CLASS_PACKAGE_PREFIX + simpleJobName
        val createTime = getCurrentDateTime()
        try {
            if (checkExists(simpleJobName, jobGroup)) {
                throw ServiceException("Job already exists, jobName:{$simpleJobName}, jobGroup:{$jobGroup}")
            }
            val triggerKey = createTriggerKey(simpleJobName, jobGroup)
            val jobKey = createJobKey(simpleJobName, jobGroup)
            val trigger = createCronTrigger(triggerKey, cronExpression, createTime)
            val clazz = Class.forName(fullJobClassName) as Class<out Job>
            val jobDetail = JobBuilder.newJob(clazz)
                .withIdentity(jobKey)
                .withDescription(jobDescription)
                .usingJobData(TASK_ID_JOB_DATA_KEY, info.id)
                .build()

            scheduler.scheduleJob(jobDetail, trigger)

            val info = TaskInfo(
                id = taskId,
                jobName = jobKey.name,
                jobGroup = jobKey.group,
                jobDescription = jobDetail.description,
                cronExpression = cronExpression,
                createTime = createTime
            )

            return ApiResponse(
                success = true,
                data = info,
                statusCode = 200,
                message = "Job added successfully."
            )

        } catch (e: ServiceException) {
            return handleServiceException(e, "Failed to add job")
        } catch (e: Exception) {
            return handleGenericException(e, "Failed to add job")
        }
    }

    fun edit(info: TaskInfo): ApiResponse {
        try {
            findTaskInfoById(info.id)
                ?: throw ServiceException("Task with ID '${info.id}' not found for editing.")

            validateTaskInfoInputs(info)
            val simpleJobName = info.jobName!!
            val jobGroup = info.jobGroup!!
            val cronExpression = info.cronExpression!!
            val jobDescription = info.jobDescription
            val createTime = getCurrentDateTime()
            val triggerKey = createTriggerKey(simpleJobName, jobGroup)
            val jobKey = createJobKey(simpleJobName, jobGroup)
            if (!scheduler.checkExists(jobKey)) {
                throw ServiceException("Job does not exist in scheduler, jobName:{$simpleJobName}, jobGroup:{$jobGroup}")
            }
            val cronTrigger = createCronTrigger(triggerKey, cronExpression, createTime)
            val jobDetail = scheduler.getJobDetail(jobKey)
            val updatedJobDetail = jobDetail.jobBuilder
                .withDescription(jobDescription)
                .usingJobData(TASK_ID_JOB_DATA_KEY, info.id)
                .build()
            
            val triggerSet = HashSet<Trigger>()
            triggerSet.add(cronTrigger)

            scheduler.scheduleJob(updatedJobDetail, triggerSet, true)

            val info = TaskInfo(
                id = info.id,
                jobName = jobKey.name,
                jobGroup = jobKey.group,
                jobDescription = jobDetail.description,
                cronExpression = cronExpression,
                createTime = createTime
            )

            return ApiResponse(
                success = true,
                data = info,
                statusCode = 200,
                message = "Job edited successfully."
            )
        } catch (e: ServiceException) {
            return handleServiceException(e, "Failed to edit job")
        } catch (e: Exception) {
            return handleGenericException(e, "Failed to edit job")
        }
    }

    fun delete(id: Int): ApiResponse {
        try {
            val (taskInfo, jobName, jobGroup) = getValidatedJobDetails(id, "deletion")
            val triggerKey = createTriggerKey(jobName, jobGroup)
            if (scheduler.checkExists(triggerKey)) {
                scheduler.pauseTrigger(triggerKey)
                scheduler.unscheduleJob(triggerKey)
                scheduler.deleteJob(createJobKey(jobName, jobGroup))
                return ApiResponse(
                    success = true,
                    data = taskInfo,
                    statusCode = 200,
                    message = "Job deleted successfully."
                )
            }

            throw ServiceException("Job to delete (associated with task ID '$id') not found in scheduler, jobName:{$jobName}, jobGroup:{$jobGroup}")
        } catch (e: ServiceException) {
            e.printStackTrace()
            return handleServiceException(e, "Failed to remove job")
        } catch (e: Exception) {
            e.printStackTrace()
            return handleGenericException(e, "Failed to remove job")
        }
    }

    fun pause(id: Int): ApiResponse {
        try {
            val (taskInfo, jobName, jobGroup) = getValidatedJobDetails(id, "pausing")
            val triggerKey = createTriggerKey(jobName, jobGroup)
            if (scheduler.checkExists(triggerKey)) {
                scheduler.pauseTrigger(triggerKey)

                return ApiResponse(
                    success = true,
                    data = taskInfo,
                    statusCode = 200,
                    message = "Job paused successfully."
                )
            }

            throw ServiceException("Job to pause (associated with task ID '$id') not found in scheduler, jobName:{$jobName}, jobGroup:{$jobGroup}")
        } catch (e: ServiceException) {
            return handleServiceException(e, "Failed to pause job")
        } catch (e: Exception) {
            return handleGenericException(e, "Failed to pause job")
        }
    }

    fun resume(id: Int): ApiResponse {
        try {
            val (taskInfo, jobName, jobGroup) = getValidatedJobDetails(id, "resuming")
            val triggerKey = createTriggerKey(jobName, jobGroup)
            if (scheduler.checkExists(triggerKey)) {
                scheduler.resumeTrigger(triggerKey)

                return ApiResponse(
                    success = true,
                    data = taskInfo,
                    statusCode = 200,
                    message = "Job resumed successfully."
                )
            }

            throw ServiceException("Job to resume (associated with task ID '$id') not found in scheduler, jobName:{$jobName}, jobGroup:{$jobGroup}")
        } catch (e: ServiceException) {
            return handleServiceException(e, "Failed to resume job")
        } catch (e: Exception) {
            return handleGenericException(e, "Failed to resume job")
        }
    }

    private fun findTaskInfoById(id: Int): TaskInfo? {
        try {
            for (groupJob in scheduler.jobGroupNames) {
                for (jobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupJob))) {
                    val jobDetail = scheduler.getJobDetail(jobKey)
                    val storedId = jobDetail.jobDataMap[TASK_ID_JOB_DATA_KEY] as? Int
                    if (storedId != null && storedId == id) {
                        // Found the job, now get its active trigger info for complete TaskInfo
                        val triggers = scheduler.getTriggersOfJob(jobKey)
                        if (triggers.isNotEmpty()) {
                            val trigger = triggers.first() // Assuming one trigger per job for simplicity in this context
                            val triggerState = scheduler.getTriggerState(trigger.key)
                            var cronExpression = ""
                            var createTime = ""
                            if (trigger is CronTrigger) {
                                cronExpression = trigger.cronExpression
                                createTime = trigger.description ?: ""
                            }
                            return TaskInfo(
                                id = storedId,
                                jobName = jobKey.name, // Simple name
                                jobGroup = jobKey.group,
                                jobDescription = jobDetail.description,
                                jobStatus = triggerState.name,
                                cronExpression = cronExpression,
                                createTime = createTime
                            )
                        }
                    }
                }
            }
        } catch (e: SchedulerException) {
            e.printStackTrace() // Log the exception
            // It's often better to log and return null or rethrow a custom exception
            // depending on how callers are expected to handle this.
            // For now, matching existing behavior of returning null.
        }
        return null
    }

    private fun validateTaskInfoInputs(info: TaskInfo) {
        info.jobName ?: throw ServiceException("Job name (simple class name) cannot be empty")
        info.jobGroup ?: throw ServiceException("Job group cannot be empty")
        info.cronExpression ?: throw ServiceException("Cron expression cannot be empty")
    }

    private fun getValidatedJobDetails(id: Int, operationContext: String): Triple<TaskInfo, String, String> {
        val taskInfo = findTaskInfoById(id)
            ?: throw ServiceException("Task with ID '$id' not found for $operationContext.")

        val jobName = taskInfo.jobName
            ?: throw ServiceException("Job name missing for task ID '$id' ($operationContext). Potential data integrity issue.")
        val jobGroup = taskInfo.jobGroup
            ?: throw ServiceException("Job group missing for task ID '$id' ($operationContext). Potential data integrity issue.")
        
        return Triple(taskInfo, jobName, jobGroup)
    }

    private fun checkExists(jobName: String, jobGroup: String): Boolean {
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
        return try {
            scheduler.checkExists(triggerKey)
        } catch (e: SchedulerException) {
            e.printStackTrace()
            false
        }
    }

    private fun handleServiceException(e: ServiceException, messagePrefix: String): ApiResponse {
        e.printStackTrace()
        return ApiResponse(
            success = false,
            statusCode = 400,
            message = "$messagePrefix: ${e.message}",
            errors = listOf(e.message ?: "Unknown service error")
        )
    }

    private fun handleGenericException(e: Exception, messagePrefix: String): ApiResponse {
        e.printStackTrace()
        return ApiResponse(
            success = false,
            statusCode = 500,
            message = "$messagePrefix: ${e.message}", // Changed from "$messagePrefix: ${e.message}"
            errors = listOf(e.message ?: "Unknown server error")
        )
    }
}
