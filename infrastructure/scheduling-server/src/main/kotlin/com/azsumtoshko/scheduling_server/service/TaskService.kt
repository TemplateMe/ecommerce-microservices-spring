package com.azsumtoshko.scheduling_server.service

import com.azsumtoshko.scheduling_server.domain.entity.TaskInfo
import com.azsumtoshko.scheduling_server.util.exception.ServiceException
import org.apache.logging.log4j.LogManager
import org.quartz.*
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.HashSet

@Service
class TaskService @Autowired constructor(
    private val scheduler: Scheduler
) {
    private val logger = LogManager.getLogger(javaClass)

    /**
     * 所有任务列表
     */
    fun list(): List<TaskInfo> {
        val list = mutableListOf<TaskInfo>()
        try {
            for (groupJob in scheduler.jobGroupNames) {
                for (jobKey in scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupJob))) {
                    val triggers = scheduler.getTriggersOfJob(jobKey)
                    for (trigger in triggers) {
                        val triggerState = scheduler.getTriggerState(trigger.key)
                        val jobDetail = scheduler.getJobDetail(jobKey)

                        var cronExpression = ""
                        var createTime = ""

                        if (trigger is CronTrigger) {
                            cronExpression = trigger.cronExpression
                            createTime = trigger.description ?: ""
                        }
                        val info = TaskInfo(
                            jobName = jobKey.name,
                            jobGroup = jobKey.group,
                            jobDescription = jobDetail.description,
                            jobStatus = triggerState.name,
                            cronExpression = cronExpression,
                            createTime = createTime
                        )
                        list.add(info)
                    }
                }
            }
        } catch (e: SchedulerException) {
            e.printStackTrace()
        }
        return list
    }

    /**
     * 保存定时任务
     */
    @Suppress("UNCHECKED_CAST")
    fun addJob(info: TaskInfo) {
        val jobName = info.jobName ?: throw ServiceException("jobName不能为空")
        val jobGroup = info.jobGroup ?: throw ServiceException("jobGroup不能为空")
        val cronExpression = info.cronExpression ?: throw ServiceException("cronExpression不能为空")
        val jobDescription = info.jobDescription
        val createTime = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(java.time.LocalDateTime.now())
        try {
            if (checkExists(jobName, jobGroup)) {
                logger.info("===> AddJob fail, job already exist, jobGroup:{}, jobName:{}", jobGroup, jobName)
                throw ServiceException("Job已经存在, jobName:{$jobName},jobGroup:{$jobGroup}")
            }

            val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
            val jobKey = JobKey.jobKey(jobName, jobGroup)

            val schedBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                .withMisfireHandlingInstructionDoNothing()
            val trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription(createTime)
                .withSchedule(schedBuilder)
                .build()

            val clazz = Class.forName(jobName) as Class<out Job>
            val jobDetail = JobBuilder.newJob(clazz)
                .withIdentity(jobKey)
                .withDescription(jobDescription)
                .build()

            scheduler.scheduleJob(jobDetail, trigger)
        } catch (e: SchedulerException) {
            throw ServiceException("类名不存在或执行表达式错误")
        } catch (e: ClassNotFoundException) {
            throw ServiceException("类名不存在或执行表达式错误")
        }
    }

    /**
     * 修改定时任务
     */
    fun edit(info: TaskInfo) {
        val jobName = info.jobName ?: throw ServiceException("jobName不能为空")
        val jobGroup = info.jobGroup ?: throw ServiceException("jobGroup不能为空")
        val cronExpression = info.cronExpression ?: throw ServiceException("cronExpression不能为空")
        val jobDescription = info.jobDescription
        val createTime = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(java.time.LocalDateTime.now())
        try {
            if (!checkExists(jobName, jobGroup)) {
                throw ServiceException("Job不存在, jobName:{$jobName},jobGroup:{$jobGroup}")
            }
            val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
            val jobKey = JobKey(jobName, jobGroup)
            val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                .withMisfireHandlingInstructionDoNothing()
            val cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription(createTime)
                .withSchedule(cronScheduleBuilder)
                .build()

            val jobDetail = scheduler.getJobDetail(jobKey)
            jobDetail.jobBuilder.withDescription(jobDescription)
            val triggerSet = HashSet<Trigger>()
            triggerSet.add(cronTrigger)

            scheduler.scheduleJob(jobDetail, triggerSet, true)
        } catch (e: SchedulerException) {
            throw ServiceException("类名不存在或执行表达式错误")
        }
    }

    /**
     * 删除定时任务
     */
    fun delete(jobName: String, jobGroup: String) {
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
        try {
            if (checkExists(jobName, jobGroup)) {
                scheduler.pauseTrigger(triggerKey)
                scheduler.unscheduleJob(triggerKey)
                logger.info("===> delete, triggerKey:{}", triggerKey)
            }
        } catch (e: SchedulerException) {
            throw ServiceException(e.message)
        }
    }

    /**
     * 暂停定时任务
     */
    fun pause(jobName: String, jobGroup: String) {
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
        try {
            if (checkExists(jobName, jobGroup)) {
                scheduler.pauseTrigger(triggerKey)
                logger.info("===> Pause success, triggerKey:{}", triggerKey)
            }
        } catch (e: SchedulerException) {
            throw ServiceException(e.message)
        }
    }

    /**
     * 重新开始任务
     */
    fun resume(jobName: String, jobGroup: String) {
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
        try {
            if (checkExists(jobName, jobGroup)) {
                scheduler.resumeTrigger(triggerKey)
                logger.info("===> Resume success, triggerKey:{}", triggerKey)
            }
        } catch (e: SchedulerException) {
            e.printStackTrace()
        }
    }

    /**
     * 验证是否存在
     */
    private fun checkExists(jobName: String, jobGroup: String): Boolean {
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)
        return scheduler.checkExists(triggerKey)
    }
}
