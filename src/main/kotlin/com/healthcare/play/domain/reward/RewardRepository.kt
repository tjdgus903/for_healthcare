package com.healthcare.play.domain.reward

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.*


interface RewardRepository: JpaRepository<Reward, UUID> {
    fun existsByAdEventId(adEventId: String): Boolean

    @Query("""
      select count(r) from Reward r 
      where r.user.id = :userId and r.createdAt between :from and :to
    """)
    fun countByUserAndCreatedBetween(userId: UUID, from: OffsetDateTime, to: OffsetDateTime): Long

    fun findTop1ByUser_IdOrderByCreatedAtDesc(userId: UUID): Reward?
}