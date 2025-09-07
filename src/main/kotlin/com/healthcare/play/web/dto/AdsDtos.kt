package com.healthcare.play.web.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.util.*

data class AdsConfigResponse(
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

data class RewardClaimRequest(
    @field:NotBlank val adNetwork: String,
    @field:NotBlank val adEventId: String,
    @field:NotBlank val rewardType: String,  // "HINT" | "EXTRA_TIME"
    @field:Min(1) val amount: Int = 1,
    val sessionId: UUID? = null,
    val proof: Map<String, Any?>? = null
)

data class RewardClaimResponse(
    val ok: Boolean,
    val newBalance: Map<String, Int>,
    val message: String?
)

data class MyInventoryResponse(
    val hintTokens: Int,
    val extraTimeTokens: Int
)