package com.healthcare.play.domain.game

import com.healthcare.play.orgs.OrgSummaryRow
import com.healthcare.play.report.GameAgg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.OffsetDateTime
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

    @Query(
        """
          select new com.healthcare.play.orgs.OrgSummaryRow(
            s.game.id,
            cast(s.game.type as string),
            count(s.id),
            avg(cast(coalesce(s.score, 0) as double)),
            avg(coalesce(s.accuracy, 0.0)),
            coalesce(sum(coalesce(s.durationSec, 0)), 0L)
          )
          from GameSession s
          where s.user.id in :userIds
            and s.startedAt between :from and :to
          group by s.game.id, s.game.type
        """
    )
    fun aggregateSummaryByGame(
        @Param("userIds") userIds: Set<UUID>,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<OrgSummaryRow>

    fun findAllByUser_IdOrderByStartedAtDesc(userId: UUID): List<GameSession>

    interface GameAggView {
        fun getGameType(): GameType
        fun getSessions(): Long
        fun getAvgScore(): Double
        fun getAvgAccuracy(): Double
        fun getSumDurationSec(): Long
        fun getLastStartedAt(): OffsetDateTime
    }

    @Query(
        """
          select
            s.game.type as gameType,
            count(s.id) as sessions,
            avg(cast(coalesce(s.score, 0) as double)) as avgScore,
            avg(coalesce(s.accuracy, 0.0)) as avgAccuracy,
            coalesce(sum(coalesce(s.durationSec, 0)), 0L) as sumDurationSec,
            max(s.startedAt) as lastStartedAt
          from GameSession s
          where s.user.id = :uid
            and s.startedAt between :from and :to
          group by s.game.type
        """
    )
    fun aggregateByGameType(
        @Param("uid") userId: UUID,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime,
    ): List<GameAggView>

    @Query(
        """
          select
             count(s.id),
             coalesce(sum(coalesce(s.durationSec,0)), 0L),
             avg(cast(coalesce(s.score,0) as double)),
             avg(coalesce(s.accuracy,0.0))
          from GameSession s
          where s.user.id = :uid
            and s.startedAt between :from and :to
        """
    )
    fun overallTotals(
        @Param("uid") userId: UUID,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime,
    ): Array<Any?>
}

interface SessionMetricRepository : JpaRepository<SessionMetric, UUID> {
    fun findAllBySessionId(sessionId: UUID): List<SessionMetric>
}