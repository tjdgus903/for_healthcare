package com.healthcare.play.subscription

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/subs")
class SubscriptionController(
    private val service: SubscriptionService
) {
    @PostMapping("/verify")
    fun verify(
        @RequestHeader("X-User-Id") userId: UUID,
        @RequestBody req: VerifyRequest
    ): ResponseEntity<VerifyResponse> = ResponseEntity.ok(service.verify(userId, req))

    @GetMapping("/status")
    fun status(
        @RequestHeader("X-User-Id") userId:UUID
    ): ResponseEntity<StatusResponse> = ResponseEntity.ok(service.status(userId))
}