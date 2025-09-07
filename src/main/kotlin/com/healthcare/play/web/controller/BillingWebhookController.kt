package com.healthcare.play.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/billing/webhook")
class BillingWebhookController {

    @PostMapping("/google")
    fun googleWebhook(@RequestBody body: String): ResponseEntity<String> {
        // TODO: GCP Pub/Sub push endpoint 구성 시 검증 + 메시지 파싱
        // - purchaseToken, subscriptionId, expiryTimeMillis 등 추출
        // - SubscriptionService 통해 갱신
        return ResponseEntity.ok("ok")
    }

    @PostMapping("/apple")
    fun appleWebhook(@RequestBody body: String,
                     @RequestHeader("Authorization", required=false) auth: String?): ResponseEntity<String> {
        // TODO: JWS 서명 검증 -> 이벤트 파싱 -> 갱신
        return ResponseEntity.ok("ok")
    }
}