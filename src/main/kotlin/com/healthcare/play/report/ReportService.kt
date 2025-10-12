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

    fun myReport(userId: UUID, rangeDays: Int = 30): MyReportResponse{
        val toInstant = Instant.now()
        val fromInstant = toInstant.minusSeconds(rangeDays.toLong() * 24 * 3600)

        val to = OffsetDateTime.ofInstant(toInstant, ZoneOffset.UTC)
        val from = OffsetDateTime.ofInstant(fromInstant, ZoneOffset.UTC)

        // 1) 반환 타입 지정
        val rows: List<GameSessionRepository.GameAggView> = gameSession.aggregateByGameType(userId, from, to)

        // 2) DTO 매핑
        val perGame: List<GameAgg> = rows.map {
            r -> GameAgg(
                gameType        = r.getGameType(),
                sessions        = r.getSessions(),
                avgScore        = r.getAvgScore(),
                avgAccuracy     = r.getAvgAccuracy(),
                sumDurationSec  = r.getSumDurationSec(),
                lastStartedAt   = r.getLastStartedAt().toInstant(),
            )
        }

        val totalsRow = gameSession.overallTotals(userId, from, to)
        val totals = Totals(
            sessions         = (totalsRow.getOrNull(0) as? Number)?.toLong() ?: 0L,
            totalDurationSec = (totalsRow.getOrNull(1) as? Number)?.toLong() ?: 0L,
            avgScore         = (totalsRow.getOrNull(2) as? Number)?.toDouble(),
            avgAccuracy      = (totalsRow.getOrNull(3) as? Number)?.toDouble()
        )

        return MyReportResponse(
            generatedAt = toInstant,
            rangeDays   = rangeDays,
            perGame     = perGame,
            totals      = totals,
        )
    }
}