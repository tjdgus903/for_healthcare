package com.healthcare.play.subscription

import com.healthcare.play.domain.user.UserRepository
import com.healthcare.play.subscription.Subscription
import com.healthcare.play.subscription.iap.*
import org.springframework.cache.annotation.CacheEvict
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
    private val props: BillingProperties,
    private val devVerifier: DevIapVerifier,
    private val gpVerifier: GooglePlayVerifier,
    private val iosVerifier: AppStoreVerifier
) {


    @CacheEvict(cacheNames = ["isSubscribed"], key = "#userId")
    @Transactional
    fun verify(userId: UUID, req: VerifyRequest): VerifyResponse {
        val user = userRepo.findById(userId).orElseThrow()
        val existing = subRepo.findByPurchaseToken(req.purchaseToken).orElse(null)

        val verifier = pickVerifier(req.devMode, req.platform)
        val result = verifier.verify(
            VerifierInput(
                platform = req.platform,
                productId = req.productId,
                purchaseToken = req.purchaseToken,
                rawPayload = req.rawPayload
            )
        )

        val sub = existing?.apply {
            this.platform = req.platform
            this.productId = req.productId
            this.status = result.status
            this.expiresAt = result.expiresAt
            this.lastVerifiedAt = Instant.now()
            this.rawPayload = req.rawPayload
        } ?: Subscription(
            user = user,
            platform = req.platform,
            productId = req.productId,
            purchaseToken = req.purchaseToken,
            status = result.status,
            startedAt = Instant.now(),
            expiresAt = result.expiresAt,
            lastVerifiedAt = Instant.now(),
            rawPayload = req.rawPayload
        )

        subRepo.save(sub)

        return VerifyResponse(
            ok = result.ok,
            status = result.status,
            expiresAt = result.expiresAt,
            message = result.note
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


    private fun pickVerifier(devMode: Boolean?, platform: Platform): IapVerifier {
        return when {
            props.devAccept || (devMode == true) -> devVerifier
            platform == Platform.ANDROID -> gpVerifier
            platform == Platform.IOS -> iosVerifier
            else -> devVerifier
        }
    }
}
