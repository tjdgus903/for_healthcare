package com.healthcare.play.web.controller

import com.healthcare.play.ads.AdsService
import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ads")
class AdsController(
    private val ads: AdsService
) {
    @GetMapping("/config")
    fun config(@AuthenticationPrincipal p: AuthPrincipal): ResponseEntity<AdsConfigResponse> {
        val c = ads.getConfig(p.userId)
        return ResponseEntity.ok(
            AdsConfigResponse(
                enabled = c.enabled,
                allowRewarded = c.allowRewarded,
                allowPostSessionAd = c.allowPostSessionAd,
                dailyRewardCap = c.dailyRewardCap,
                cooldownSeconds = c.cooldownSeconds,
                allowedRewardTypes = c.allowedRewardTypes,
                subscribed = c.subscribed,
                todayEarned = c.todayEarned,
                todayRemain = c.todayRemain
            )
        )
    }

    @PostMapping("/rewards/claim")
    fun claim(
        @AuthenticationPrincipal p: AuthPrincipal,
        @RequestBody @Valid req: RewardClaimRequest
    ): ResponseEntity<RewardClaimResponse> {
        val r = ads.claim(p.userId, AdsService.ClaimRequest(
            adNetwork = req.adNetwork,
            adEventId = req.adEventId,
            rewardType = req.rewardType,
            amount = req.amount,
            sessionId = req.sessionId,
            proof = req.proof
        ))
        return ResponseEntity.ok(RewardClaimResponse(r.ok, r.newBalance, r.message))
    }

    @GetMapping("/inventory/me")
    fun myInventory(@AuthenticationPrincipal p: AuthPrincipal): MyInventoryResponse {
        val m = ads.myInventory(p.userId)
        return MyInventoryResponse(
            hintTokens = m["hintTokens"] ?: 0,
            extraTimeTokens = m["extraTimeTokens"] ?: 0
        )
    }

    @PostMapping("/rewards/spend")
    fun spend(
        @AuthenticationPrincipal p: AuthPrincipal,
        @RequestBody @Valid req: TokenSpendRequest
    ): ResponseEntity<TokenSpendResponse> {
        val r = ads.spend(p.userId, AdsService.SpendRequest(req.rewardType, req.amount))
        return ResponseEntity.ok(TokenSpendResponse(r.ok, r.newBalance, r.message))
    }
}