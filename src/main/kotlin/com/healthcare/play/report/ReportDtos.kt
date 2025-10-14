package com.healthcare.play.report

import com.healthcare.play.domain.game.GameType
import java.time.Instant

// 1개 게임 타입에 대한 집계
data class GameAgg(
    val gameType: GameType,    // (1) s.game.type이 Enum이면 Enum로 받으세요. String이 아님!
    val sessions: Long,        // (2) count -> Long
    val avgScore: Double,      // (3) avg -> Double
    val avgAccuracy: Double,   // (4) avg -> Double
    val sumDurationSec: Long,  // (5) sum -> Long
    val lastStartedAt: Instant // (6) max -> Instant
)

// /report/me 응답
data class MyReportResponse(
    val generatedAt: Instant,
    val rangeDays: Int,
    val perGame: List<GameAgg>,
    val totals: Totals,
    val byStringMeta: Map<String, List<MetaStringRow>>,  // "level" → [{gameType, metaVal, sessions, ...}]
    val byNumericMeta: Map<String, List<MetaNumericRow>>  // "total" → [{gameType, avgValue, ...}]
)

data class MetaStringRow(
    val gameType: String,
    val metaVal: String,
    val sessions: Long,
    val avgScore: Double,
    val avgAccuracy: Double,
    val sumDurationSec: Long
)

data class MetaNumericRow(
    val gameType: String,
    val avgValue: Double,
    val minValue: Double,
    val maxValue: Double
)

data class Totals(
    val sessions: Long,
    val totalDurationSec: Long,
    val avgScore: Double?,
    val avgAccuracy: Double?
)