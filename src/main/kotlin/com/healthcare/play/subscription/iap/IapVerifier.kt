package com.healthcare.play.subscription.iap

import com.healthcare.play.subscription.Platform
import com.healthcare.play.subscription.SubStatus
import java.time.Instant

data class VerifierInput(
    val platform: Platform,
    val productId: String,
    val purchaseToken: String, // Android: purchaseToken / iOS: transactionId or signedJWS
    val rawPayload: String? = null
)

data class VerifierResult(
    val ok: Boolean,
    val status: SubStatus,         // ACTIVE / CANCELED / EXPIRED
    val expiresAt: Instant?,       // null이면 만료정보 없음
    val note: String? = null       // dev-accept, store-response-code 등
)

interface IapVerifier {
    fun verify(input: VerifierInput): VerifierResult
}