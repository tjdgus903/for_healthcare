package com.healthcare.play.report

import com.healthcare.play.domain.game.GameRepository
import com.healthcare.play.domain.game.GameSessionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class ReportService(
    private val gameSession: GameSessionRepository,
) {

    fun myReport(userId: UUID, rangeDays: Int = 30): MyReportResponse {
        val toInstant = Instant.now()
        val fromInstant = toInstant.minusSeconds(rangeDays.toLong() * 24 * 3600)

        val to   = OffsetDateTime.ofInstant(toInstant, ZoneOffset.UTC)
        val from = OffsetDateTime.ofInstant(fromInstant, ZoneOffset.UTC)

        // 1) 게임별 집계(프로젝션 → DTO)
        val rows = gameSession.aggregateByGameType(userId, from, to)
        val perGame = rows.map { r ->
            GameAgg(
                gameType       = r.getGameType(),
                sessions       = r.getSessions(),
                avgScore       = r.getAvgScore(),
                avgAccuracy    = r.getAvgAccuracy(),
                sumDurationSec = r.getSumDurationSec(),
                lastStartedAt  = r.getLastStartedAt().toInstant(),
            )
        }

        // 2) 메타 집계(문자열/숫자 키)
        val keysString  = listOf("level", "mode", "palette")
        val keysNumeric = listOf("total", "correct")

        val byStringMeta: Map<String, List<MetaStringRow>> =
            keysString.associateWith { key ->
                gameSession.aggregateByMetaString(userId, from, to, key).map {
                    MetaStringRow(
                        gameType       = it.getGameType(),
                        metaVal        = it.getMetaVal(),
                        sessions       = it.getSessions(),
                        avgScore       = it.getAvgScore(),
                        avgAccuracy    = it.getAvgAccuracy(),
                        sumDurationSec = it.getSumDurationSec()
                    )
                }
            }

        val byNumericMeta: Map<String, List<MetaNumericRow>> =
            keysNumeric.associateWith { key ->
                gameSession.aggregateByMetaNumeric(userId, from, to, key).map {
                    MetaNumericRow(
                        gameType = it.getGameType(),
                        avgValue = it.getAvgValue(),
                        minValue = it.getMinValue(),
                        maxValue = it.getMaxValue(),
                    )
                }
            }

        // 3) 전체 합계
        val totalsRow = gameSession.overallTotals(userId, from, to)
        val totals = Totals(
            sessions         = (totalsRow.getOrNull(0) as? Number)?.toLong() ?: 0L,
            totalDurationSec = (totalsRow.getOrNull(1) as? Number)?.toLong() ?: 0L,
            avgScore         = (totalsRow.getOrNull(2) as? Number)?.toDouble() ?: 0.0,
            avgAccuracy      = (totalsRow.getOrNull(3) as? Number)?.toDouble() ?: 0.0
        )

        return MyReportResponse(
            generatedAt   = toInstant,
            rangeDays     = rangeDays,
            perGame       = perGame,
            totals        = totals,
            byStringMeta  = byStringMeta,
            byNumericMeta = byNumericMeta
        )
    }
}