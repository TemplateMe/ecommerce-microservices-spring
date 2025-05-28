package com.azsumtoshko.scheduling_server.job

import com.azsumtoshko.scheduling_server.util.HttpJobExecutor
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.quartz.QuartzJobBean


@DisallowConcurrentExecution
class HttpJob : QuartzJobBean() {

    companion object {
        lateinit var context: ApplicationContext
    }

    override fun executeInternal(jobContext: JobExecutionContext) {
        val jobData = jobContext.jobDetail.jobDataMap
        val url = jobData.getString("url") ?: return
        val method = jobData.getString("method") ?: "GET"
        val headers = jobData.getString("headers")
        val body = jobData.getString("body")

        val executor = context.getBean(HttpJobExecutor::class.java)
        executor.execute(url, method, headers, body)
    }
}