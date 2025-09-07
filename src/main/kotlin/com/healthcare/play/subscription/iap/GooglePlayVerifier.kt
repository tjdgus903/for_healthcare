package com.healthcare.play.subscription.iap

import com.healthcare.play.subscription.SubStatus
import org.springframework.stereotype.Component

@Component
class GooglePlayVerifier : IapVerifier {
    override fun verify(input: VerifierInput): VerifierResult {
        // TODO: Google Play Developer API 호출
        // purchases.subscriptions.get(packageName, subscriptionId, purchaseToken)
        // 응답 -> status/만료일 파싱
        return VerifierResult(
            ok = false,
            status = SubStatus.EXPIRED,
            expiresAt = null,
            note = "store-verify-not-implemented"
        )
    }
}