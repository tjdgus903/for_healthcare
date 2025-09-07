package com.healthcare.play.ads

import com.healthcare.play.domain.inventory.UserInventory
import com.healthcare.play.domain.inventory.UserInventoryRepository
import com.healthcare.play.domain.reward.Reward
import com.healthcare.play.domain.reward.RewardRepository
import com.healthcare.play.domain.user.UserRepository.UserRepository
import com.healthcare.play.subscription.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.math.max

@Service
class AdsService(
    private val props: AdsProperties,
    private val userRepo: UserRepository,
    private val invRepo: UserInventoryRepository,
    private val rewardRepo: RewardRepository,
    private val subscriptionService: SubscriptionService
) {
    data class AdsConfigDto(
        val enabled: Boolean,
        val allowRewarded: Boolean,
        val allowPostSessionAd: Boolean,
        val dailyRewardCap: Int,
        val cooldownSeconds: Int,
        val allowedRewardTypes: List<String>,
        val subscribed: Boolean,
        val todayEarned: Int,
        val todayRemain: Int
    )

    data class SpendRequest(
        val rewardType: String,   // "HINT" | "EXTRA_TIME"
        val amount: Int = 1
    )
    data class SpendResult(
        val ok: Boolean,
        val newBalance: Map<String, Int>,
        val message: String? = null
    )

    @Transactional(readOnly = true)
    fun getConfig(userId: UUID): AdsConfigDto {
        if (!props.enabled) {
            return AdsConfigDto(false, false, false, 0, 0, emptyList(), false, 0, 0)
        }
        val subscribed = subscriptionService.isSubscribed(userId)
        val (earned, remain) = todayCountAndRemain(userId)

        return AdsConfigDto(
            enabled = true,
            allowRewarded = props.allowRewarded && !subscribed,
            allowPostSessionAd = props.allowPostSessionAd && !subscribed,
            dailyRewardCap = props.dailyRewardCap,
            cooldownSeconds = props.cooldownSeconds,
            allowedRewardTypes = props.allowedRewardTypes,
            subscribed = subscribed,
            todayEarned = earned,
            todayRemain = remain
        )
    }

    private fun todayCountAndRemain(userId: UUID): Pair<Int, Int> {
        val today = LocalDate.now(ZoneOffset.UTC)
        val from = today.atStartOfDay().atOffset(ZoneOffset.UTC)
        val to = today.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
        val count = rewardRepo.countByUserAndCreatedBetween(userId, from, to).toInt()
        val remain = max(0, props.dailyRewardCap - count)
        return count to remain
    }

    data class ClaimRequest(
        val adNetwork: String,       // "admob" 등
        val adEventId: String,       // 광고SDK에서 제공하는 고유 이벤트ID(중복 방지)
        val rewardType: String,      // "HINT" | "EXTRA_TIME"
        val amount: Int = 1,
        val sessionId: UUID? = null,
        val proof: Map<String, Any?>? = null  // 개발 단계에서는 미검증(추후 영수증 검증)
    )
    data class ClaimResult(
        val ok: Boolean,
        val newBalance: Map<String, Int>,     // hintTokens, extraTimeTokens
        val message: String? = null
    )

    @Transactional
    fun claim(userId: UUID, req: ClaimRequest): ClaimResult {
        if (!props.enabled) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Ads disabled")
        if (subscriptionService.isSubscribed(userId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Subscribed user: ads disabled")
        }
        if (!props.allowRewarded) throw ResponseStatusException(HttpStatus.FORBIDDEN, "Rewarded ads disabled")

        // 허용 타입 검증
        if (!props.allowedRewardTypes.contains(req.rewardType)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reward type not allowed")
        }

        // 일일 한도
        val (earned, remain) = todayCountAndRemain(userId)
        if (remain <= 0) throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily cap reached")

        // 쿨다운
        val last = rewardRepo.findTop1ByUser_IdOrderByCreatedAtDesc(userId)
        if (last?.createdAt != null) {
            val seconds = OffsetDateTime.now().toEpochSecond() - last.createdAt!!.toEpochSecond()
            if (seconds < props.cooldownSeconds) {
                throw ResponseStatusException(HttpStatus.TOO_EARLY, "Cooldown ${props.cooldownSeconds - seconds}s")
            }
        }

        // 중복 이벤트 방지
        if (req.adEventId.isNotBlank() && rewardRepo.existsByAdEventId(req.adEventId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Duplicate ad event")
        }

        // (선택) proof 검증은 M6에서는 devMode면 스킵, 운영에서 구현
        if (!props.devMode) {
            // TODO: 네트워크 서명/콜백 검증 구현(운영용)
        }

        // 인벤토리 확보/생성
        val user = userRepo.findById(userId).orElseThrow()
        val inv = invRepo.findByUser_Id(userId).orElseGet {
            invRepo.save(UserInventory(user = user))
        }

        when (req.rewardType) {
            "HINT" -> inv.hintTokens += req.amount
            "EXTRA_TIME" -> inv.extraTimeTokens += req.amount
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown reward type")
        }
        invRepo.save(inv)

        // 보상 레코드 저장
        rewardRepo.save(Reward(
            user = user,
            rewardType = req.rewardType,
            amount = req.amount,
            adNetwork = req.adNetwork,
            adEventId = req.adEventId,
            sessionId = req.sessionId
        ))

        return ClaimResult(
            ok = true,
            newBalance = mapOf(
                "hintTokens" to inv.hintTokens,
                "extraTimeTokens" to inv.extraTimeTokens
            ),
            message = "Reward granted"
        )
    }

    @Transactional(readOnly = true)
    fun myInventory(userId: UUID): Map<String, Int> {
        val inv: UserInventory? = invRepo.findByUser_Id(userId).orElse(null)
        return if (inv == null) {
            mapOf("hintTokens" to 0, "extraTimeTokens" to 0)
        } else {
            mapOf("hintTokens" to inv.hintTokens, "extraTimeTokens" to inv.extraTimeTokens)
        }
    }

    @Transactional
    fun spend(userId: UUID, req: SpendRequest): SpendResult {
        val user = userRepo.findById(userId).orElseThrow()
        val inv = invRepo.findByUser_Id(userId).orElseGet { invRepo.save(
            com.healthcare.play.domain.inventory.UserInventory(user = user)
        ) }

        when (req.rewardType) {
            "HINT" -> {
                if (inv.hintTokens < req.amount)
                    throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Not enough HINT tokens")
                inv.hintTokens -= req.amount
            }
            "EXTRA_TIME" -> {
                if (inv.extraTimeTokens < req.amount)
                    throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Not enough EXTRA_TIME tokens")
                inv.extraTimeTokens -= req.amount
            }
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown reward type")
        }
        invRepo.save(inv)

        return SpendResult(
            ok = true,
            newBalance = mapOf(
                "hintTokens" to inv.hintTokens,
                "extraTimeTokens" to inv.extraTimeTokens
            ),
            message = "Token spent"
        )
    }
}