package com.healthcare.play.web

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ApiError(val timestamp: Instant = Instant.now(), val status: Int, val error: String, val message: String?)

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException::class, IllegalArgumentException::class)
    fun notFound(e: RuntimeException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(status = 400, error = "BAD_REQUEST", message = e.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun invalid(e: MethodArgumentNotValidException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(status = 400, error = "VALIDATION_ERROR", message = e.bindingResult.allErrors.joinToString { it.defaultMessage ?: "" }))

    @ExceptionHandler(Exception::class)
    fun generic(e: Exception): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(status = 500, error = "INTERNAL_ERROR", message = e.message))

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun forbidden(e: org.springframework.security.access.AccessDeniedException) =
        ResponseEntity.status(403).body(ApiError(status=403, error="FORBIDDEN", message=e.message))
}