package com.healthcare.play.subscription

import com.healthcare.play.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class VerifyRequest(
    val platform: Platform,        // ANDROID | IOS
    val productId: String,
    val purchaseToken: String,
    val devMode: Boolean? = null,
    val rawPayload: String? = null
)
data class VerifyResponse(
    val ok: Boolean,
    val status: SubStatus,
    val expiresAt: Instant?,
    val message: String? = null
)
data class StatusResponse(
    val subscribed: Boolean,
    val status: SubStatus?,
    val productId: String?,
    val expiresAt: Instant?
)

@Service
class SubscriptionService(
    private val userRepo: UserRepository,
    private val subRepo: SubscriptionRepository,
    private val props: BillingProperties
) {

    @Transactional
    fun verify(userId: UUID, req: VerifyRequest): VerifyResponse {
        val user = userRepo.findById(userId).orElseThrow()

        // 스텁: devAccept 또는 요청 devMode=true 면 강제 통과
        val (verifiedStatus, verifiedExpire, note) =
            if (props.devAccept || (req.devMode == true)) {
                SubStatus.ACTIVE to Instant.now().plus(props.defaultDays, ChronoUnit.DAYS) to "dev-accept"
            } else {
                SubStatus.EXPIRED to null to "store-verify-not-implemented"
            }.let {
                Triple(it.first.first, it.first.second, it.second)
            }

        val existing = subRepo.findByPurchaseToken(req.purchaseToken).orElse(null)

        val sub = existing?.apply {
            this.platform = req.platform
            this.productId = req.productId
            this.status = verifiedStatus
            this.expiresAt = verifiedExpire
            this.lastVerifiedAt = Instant.now()
            this.rawPayload = req.rawPayload
        } ?: Subscription(
            user = user,
            platform = req.platform,
            productId = req.productId,
            purchaseToken = req.purchaseToken,
            status = verifiedStatus,
            startedAt = Instant.now(),
            expiresAt = verifiedExpire,
            lastVerifiedAt = Instant.now(),
            rawPayload = req.rawPayload
        )

        subRepo.save(sub)

        return VerifyResponse(
            ok = (verifiedStatus == SubStatus.ACTIVE),
            status = verifiedStatus,
            expiresAt = verifiedExpire,
            message = note
        )
    }

    @Transactional(readOnly = true)
    fun status(userId: UUID): StatusResponse {
        val now = Instant.now()
        val latest = subRepo.findTopByUser_IdOrderByExpiresAtDesc(userId).orElse(null)
        val active = latest != null && latest.status == SubStatus.ACTIVE && (latest.expiresAt ?: now).isAfter(now)
        return StatusResponse(
            subscribed = active,
            status = latest?.status,
            productId = latest?.productId,
            expiresAt = latest?.expiresAt
        )
    }

    @Transactional(readOnly = true)
    fun isSubscribed(userId: UUID): Boolean = status(userId).subscribed
}
