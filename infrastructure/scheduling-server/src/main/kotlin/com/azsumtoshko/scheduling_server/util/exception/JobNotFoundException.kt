package com.azsumtoshko.scheduling_server.util.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class JobNotFoundException(
    override val message: String = "Scheduled Job not found"
) : RuntimeException(message)