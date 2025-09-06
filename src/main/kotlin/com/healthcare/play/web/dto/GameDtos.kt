package com.healthcare.play.web.dto

import com.healthcare.play.domain.game.GameType
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.*

data class GameSimple(
    val id: UUID, val type: GameType, val name: String, val description: String
)

data class StartSessionRequest(
    @field:NotNull val gameType: GameType,
    val meta: Map<String, Any>? = null
)
data class StartSessionResponse(
    val sessionId: UUID, val gameType: GameType, val startedAt: OffsetDateTime
)

data class AddMetricsRequest(
    val metrics: List<MetricItemDto>
)
data class MetricItemDto(
    val key: String, val value: Double, val unit: String? = null, val extra: Map<String, Any>? = null
)

data class EndSessionRequest(
    val score: Int? = null,
    val accuracy: Double? = null,
    val durationSec: Int? = null,
    val meta: Map<String, Any>? = null
)

data class SessionSummary(
    val sessionId: UUID,
    val gameType: GameType,
    val startedAt: OffsetDateTime,
    val endedAt: OffsetDateTime?,
    val durationSec: Int?,
    val score: Int?,
    val accuracy: Double?
)