package com.healthcare.play.subscription

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.*

interface SubscriptionRepository : JpaRepository<Subscription, UUID> {
    fun findTopByUser_IdOrderByExpiresAtDesc(userId: UUID): Optional<Subscription>
    fun findByPurchaseToken(purchaseToken: String): Optional<Subscription>
    fun countByUser_IdAndStatusAndExpiresAtAfter(userId: UUID, status: SubStatus, now: Instant): Long
}
