package io.hhplus.tdd.application.common

import org.springframework.http.HttpStatus

open class BusinessException(
    val code: String,
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)