package com.healthcare.play.subscription.iap

import com.healthcare.play.subscription.BillingProperties
import com.healthcare.play.subscription.SubStatus
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class DevIapVerifier(private val props: BillingProperties) : IapVerifier {
    override fun verify(input: VerifierInput): VerifierResult {
        return VerifierResult(
            ok = true,
            status = SubStatus.ACTIVE,
            expiresAt = Instant.now().plus(props.defaultDays, ChronoUnit.DAYS),
            note = "dev-accept"
        )
    }
}