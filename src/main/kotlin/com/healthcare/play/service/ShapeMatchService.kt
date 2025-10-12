package com.healthcare.play.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.healthcare.play.domain.game.GameSessionRepository
import com.healthcare.play.domain.game.GameType
import com.healthcare.play.domain.game.SessionMetric
import com.healthcare.play.domain.game.SessionMetricRepository
import com.healthcare.play.service.shape.ShapeLayoutGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.max

@Service
class ShapeMatchService(
    private val sessions: GameSessionService,
    private val sessionRepo: GameSessionRepository,
    private val metricRepo: SessionMetricRepository
) {
    private val M = jacksonObjectMapper()

    data class StartOptions(
        val rows: Int = 2,
        val cols: Int = 2,
        val totalRounds: Int = 10,
        val shapeSet: List<String> = listOf("CIRCLE","TRIANGLE","SQUARE","STAR"),
        val inputTimeoutMs: Int = 5000
    )

    data class StartResult(
        val sessionId: UUID,
        val seed: Long,
        val rows: Int,
        val cols: Int,
        val totalRounds: Int,
        val shapeSet: List<String>,
        val roundIndex: Int,
        val target: String,
        val cells: List<String>,     // rows*cols
        val inputTimeoutMs: Int
    )

    @Transactional
    fun start(userId: UUID, opt: StartOptions): StartResult {
        val seed = System.currentTimeMillis()
        val meta = mapOf(
            "seed" to seed,
            "rows" to opt.rows,
            "cols" to opt.cols,
            "totalRounds" to opt.totalRounds,
            "shapeSet" to opt.shapeSet,
            "inputTimeoutMs" to opt.inputTimeoutMs
        )
        val s = sessions.start(userId, GameType.SHAPE_MATCH, meta)
        val first = ShapeLayoutGenerator.generate(seed, 1, opt.shapeSet, opt.rows, opt.cols)
        return StartResult(
            sessionId = s.id!!,
            seed = seed,
            rows = opt.rows, cols = opt.cols,
            totalRounds = opt.totalRounds,
            shapeSet = opt.shapeSet,
            roundIndex = 1,
            target = first.target,
            cells = first.cells,
            inputTimeoutMs = opt.inputTimeoutMs
        )
    }

    data class LayoutResponse(
        val roundIndex: Int,
        val target: String,
        val cells: List<String>
    )

    @Transactional(readOnly = true)
    fun getLayout(sessionId: UUID, roundIndex: Int): LayoutResponse {
        val s = sessionRepo.findById(sessionId).orElseThrow()
        val meta = M.readValue(s.meta, Map::class.java)
        val seed = (meta["seed"] as Number).toLong()
        val rows = (meta["rows"] as Number).toInt()
        val cols = (meta["cols"] as Number).toInt()
        val shapeSet = (meta["shapeSet"] as List<*>).map { it.toString() }
        val layout = ShapeLayoutGenerator.generate(seed, roundIndex, shapeSet, rows, cols)
        return LayoutResponse(roundIndex, layout.target, layout.cells)
    }

    data class SubmitRequest(
        val roundIndex: Int,
        val chosenIndex: Int,   // 사용자가 클릭한 셀 index(0..rows*cols-1)
        val reactionMs: Int? = null
    )
    data class SubmitResult(
        val roundIndex: Int,
        val correct: Boolean,
        val targetIndex: Int,
        val finished: Boolean,
        val nextRound: Int?
    )

    @Transactional
    fun submit(sessionId: UUID, req: SubmitRequest): SubmitResult {
        val s = sessionRepo.findById(sessionId).orElseThrow()
        val meta = M.readValue(s.meta, Map::class.java)
        val seed = (meta["seed"] as Number).toLong()
        val rows = (meta["rows"] as Number).toInt()
        val cols = (meta["cols"] as Number).toInt()
        val total = (meta["totalRounds"] as Number).toInt()
        val shapeSet = (meta["shapeSet"] as List<*>).map { it.toString() }

        val layout = ShapeLayoutGenerator.generate(seed, req.roundIndex, shapeSet, rows, cols)

        // 타깃의 index(최초 등장 자리)
        val targetIndex = layout.cells.indexOf(layout.target)

        val correct = (req.chosenIndex == targetIndex)

        // 라운드 메트릭
        metricRepo.save(SessionMetric(
            session = s,
            key = if (correct) "round_hit" else "round_miss",
            value = req.roundIndex.toDouble(),
            unit = null,
            extra = M.writeValueAsString(mapOf(
                "chosenIndex" to req.chosenIndex,
                "targetIndex" to targetIndex,
                "reactionMs" to (req.reactionMs ?: -1)
            ))
        ))

        val finished = !correct || req.roundIndex >= total
        val next = if (finished) null else req.roundIndex + 1

        return SubmitResult(req.roundIndex, correct, targetIndex, finished, next)
    }

    data class EndPayload(
        val totalRounds: Int,
        val hitCount: Int,
        val missCount: Int,
        val avgReactionMs: Int? = null
    )

    @Transactional
    fun end(sessionId: UUID, payload: EndPayload): com.healthcare.play.web.dto.SessionSummary {
        // 간단 점수: hit*20 - miss*5 (최소 0), 정확도 = hit / (hit+miss)
        val raw = payload.hitCount * 20 - payload.missCount * 5
        val score = max(0, raw)
        val accuracy = if (payload.hitCount + payload.missCount == 0) null
        else payload.hitCount.toDouble() / (payload.hitCount + payload.missCount)

        val s = sessionRepo.findById(sessionId).orElseThrow()
        metricRepo.save(SessionMetric(session = s, key = "summary_hit", value = payload.hitCount.toDouble()))
        metricRepo.save(SessionMetric(session = s, key = "summary_miss", value = payload.missCount.toDouble()))
        payload.avgReactionMs?.let {
            metricRepo.save(SessionMetric(session = s, key = "summary_avgReactionMs", value = it.toDouble(), unit="ms"))
        }

        val ended = sessions.end(
            sessionId = sessionId,
            score = score,
            accuracy = accuracy,
            durationSec = null,
            meta = mapOf(
                "totalRounds" to payload.totalRounds,
                "hitCount" to payload.hitCount,
                "missCount" to payload.missCount,
                "avgReactionMs" to (payload.avgReactionMs ?: -1)
            )
        )
        return com.healthcare.play.web.dto.SessionSummary(
            sessionId = ended.id!!,
            gameType = ended.game.type,
            startedAt = ended.startedAt,
            endedAt = ended.endedAt,
            durationSec = ended.durationSec,
            score = ended.score,
            accuracy = ended.accuracy
        )
    }
}