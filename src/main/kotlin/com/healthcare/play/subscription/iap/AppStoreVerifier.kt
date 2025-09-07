package com.healthcare.play.subscription.iap

import com.healthcare.play.subscription.SubStatus
import org.springframework.stereotype.Component

@Component
class AppStoreVerifier : IapVerifier {
    override fun verify(input: VerifierInput): VerifierResult {
        // TODO: App Store Server API 호출 (signed JWS 검증)
        return VerifierResult(
            ok = false,
            status = SubStatus.EXPIRED,
            expiresAt = null,
            note = "store-verify-not-implemented"
        )
    }
}