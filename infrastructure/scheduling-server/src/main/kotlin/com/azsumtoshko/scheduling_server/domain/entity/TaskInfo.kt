package com.azsumtoshko.scheduling_server.domain.entity

import java.io.Serializable

data class TaskInfo(
    var id: Int = 0,
    var jobName: String? = null,
    var jobGroup: String? = null,
    var jobDescription: String? = null,
    var jobStatus: String? = null,
    var cronExpression: String? = null,
    var createTime: String? = null
) : Serializable