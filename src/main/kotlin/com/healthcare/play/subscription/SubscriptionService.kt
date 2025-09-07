package com.healthcare.play.subscription

import com.healthcare.play.domain.user.UserRepository
import com.healthcare.play.subscription.Subscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class VerifyRequest(
    val platform: Platform,        // ANDROID | IOS
    val productId: String,
    val purchaseToken: String,
    val devMode: Boolean? = null,  // true면 스텁 처리 강제
    val rawPayload: String? = null // 클라이언트가 받은 영수증/응답도 보관 가능
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

        // 중복/재검증 방지: 같은 purchaseToken이 있으면 갱신만
        val existing = subRepo.findByPurchaseToken(req.purchaseToken).orElse(null)

        val (verifiedStatus, verifiedExpire, note) = if (props.devAccept || (req.devMode == true)) {
            // 개발용 스텁: now + defaultDays
            Triple(SubStatus.ACTIVE, Instant.now().plus(props.defaultDays, ChronoUnit.DAYS), "dev-accept")
        } else {
            // TODO: 실제 스토어 서버 검증 로직
            // - ANDROID: Google Play Developer API (Purchases.subscriptions.get)
            // - IOS: App Store Server API (JWSToken -> verifyTransaction)
            // 여기서는 실패로 처리 예시
            Triple(SubStatus.EXPIRED, null, "store-verify-not-implemented")
        }

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
        // 가장 최근 구독 하나로 판단(단순화)
        val latest = subRepo.findTopByUser_IdOrderByExpiresAtDesc(userId).orElse(null)
        val active = latest != null && latest.status == SubStatus.ACTIVE && (latest.expiresAt ?: now).isAfter(now)
        return StatusResponse(
            subscribed = active,
            status = latest?.status,
            productId = latest?.productId,
            expiresAt = latest?.expiresAt
        )
    }

    /** M6 등에서 '광고 OFF' 판정에 사용 */
    @Transactional(readOnly = true)
    fun isSubscribed(userId: UUID): Boolean = status(userId).subscribed
}
