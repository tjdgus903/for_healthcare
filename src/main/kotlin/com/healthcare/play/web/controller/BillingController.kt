package com.healthcare.play.web.controller

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.subscription.StatusResponse
import com.healthcare.play.subscription.SubscriptionService
import com.healthcare.play.subscription.VerifyRequest
import com.healthcare.play.subscription.VerifyResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/billing")
class BillingController(
    private val svc: SubscriptionService
) {
    @PostMapping("/verify")
    fun verify(
        @AuthenticationPrincipal p: AuthPrincipal,
        @RequestBody @Valid req: VerifyRequest
    ): ResponseEntity<VerifyResponse> {
        val r = svc.verify(p.userId, req)
        return ResponseEntity.ok(r)
    }

    @GetMapping("/status")
    fun status(@AuthenticationPrincipal p: AuthPrincipal): ResponseEntity<StatusResponse> {
        return ResponseEntity.ok(svc.status(p.userId))
    }
}
