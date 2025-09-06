package com.healthcare.play.domain.game

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameRepository : JpaRepository<Game, UUID> {
    fun findByType(type: GameType): Game?
    fun findAllByActiveTrueOrderByNameAsc(): List<Game>
}

interface GameSessionRepository : JpaRepository<GameSession, UUID> {
    fun findAllByUserIdOrderByStartedAtDesc(userId: UUID): List<GameSession>
    fun findAllByUserIdAndGameTypeOrderByStartedAtDesc(userId: UUID, type: GameType): List<GameSession>
}

interface SessionMetricRepository : JpaRepository<SessionMetric, UUID> {
    fun findAllBySessionId(sessionId: UUID): List<SessionMetric>
}