package io.hhplus.tdd.domain

import org.springframework.http.HttpStatus

open class BusinessException(
    val code: String,
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)