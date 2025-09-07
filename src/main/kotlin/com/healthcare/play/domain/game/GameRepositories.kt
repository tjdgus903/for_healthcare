package com.healthcare.play.domain.game

import com.healthcare.play.orgs.OrgSummaryRow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface GameRepository : JpaRepository<Game, UUID> {
    fun findByType(type: GameType): Game?
    fun findAllByActiveTrueOrderByNameAsc(): List<Game>
}

interface GameSessionRepository : JpaRepository<GameSession, UUID> {
    fun findAllByUserIdOrderByStartedAtDesc(userId: UUID): List<GameSession>
    fun findAllByUser_IdAndGame_TypeOrderByStartedAtDesc(
        userId: UUID,
        type: GameType
    ): List<GameSession>
    @Query("""
        select new com.healthcare.play.orgs.OrgSummaryRow(
          s.game.id,
          cast(s.game.type as string),
          count(s.id),
          avg(s.score),
          avg(s.accuracy),
          coalesce(sum(s.durationSec), 0)
        )
        from GameSession s
        where s.user.id in :userIds
          and s.startedAt between :from and :to
        group by s.game.id, s.game.type
    """)
    fun aggregateSummaryByGame(
        @Param("userIds") userIds: Set<UUID>,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<OrgSummaryRow>

    fun findAllByUser_IdOrderByStartedAtDesc(userId: UUID): List<GameSession>
}

interface SessionMetricRepository : JpaRepository<SessionMetric, UUID> {
    fun findAllBySessionId(sessionId: UUID): List<SessionMetric>
}