package com.healthcare.play.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.healthcare.play.domain.game.GameType
import com.healthcare.play.domain.game.SessionMetricRepository
import com.healthcare.play.domain.game.GameSessionRepository
import com.healthcare.play.service.sequence.SequenceGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.max

@Service
class SequenceMemoryService(
    private val sessions: GameSessionService,
    private val sessionRepo: GameSessionRepository,
    private val metricRepo: SessionMetricRepository
) {
    private val M = jacksonObjectMapper()

    data class StartOptions(
        val totalRounds: Int = 8,                 // 라운드 수 (3~?)
        val symbolSet: List<String> = listOf("R","G","B","Y"), // 사용할 심볼 셋(색상 코드 등)
        val showMs: Int = 800,                    // 보여주기 속도(다음 라운드로 갈수록 줄일 수도)
        val inputTimeoutMs: Int = 6000            // 입력 제한 시간(프론트 검증용)
    )

    data class StartResult(
        val sessionId: UUID,
        val seed: Long,
        val totalRounds: Int,
        val symbolSet: List<String>,
        val roundIndex: Int,           // 1부터 시작
        val pattern: List<String>,     // 첫 라운드 패턴
        val showMs: Int,
        val inputTimeoutMs: Int
    )

    @Transactional
    fun start(userId: UUID, opt: StartOptions): StartResult {
        val seed = System.currentTimeMillis()
        val meta = mapOf(
            "seed" to seed,
            "totalRounds" to opt.totalRounds,
            "symbolSet" to opt.symbolSet,
            "showMs" to opt.showMs,
            "inputTimeoutMs" to opt.inputTimeoutMs
        )
        val s = sessions.start(userId, GameType.SEQUENCE_MEMORY, meta)
        val first = SequenceGenerator.generate(seed, 1, opt.symbolSet)
        return StartResult(
            sessionId = s.id!!,
            seed = seed,
            totalRounds = opt.totalRounds,
            symbolSet = opt.symbolSet,
            roundIndex = 1,
            pattern = first,
            showMs = opt.showMs,
            inputTimeoutMs = opt.inputTimeoutMs
        )
    }

    data class NextRoundResponse(
        val roundIndex: Int,
        val pattern: List<String>
    )

    /** 프론트가 패턴을 잊었을 때 재요청(프론트에서 캐시 못했을 때 대비) */
    @Transactional(readOnly = true)
    fun getPattern(sessionId: UUID, roundIndex: Int): NextRoundResponse {
        val s = sessionRepo.findById(sessionId).orElseThrow()
        val meta = s.meta?.let { M.readValue(it, Map::class.java) } as Map<*, *>
        val seed = (meta["seed"] as Number).toLong()
        val symbolSet = (meta["symbolSet"] as List<*>).map { it.toString() }
        val pattern = SequenceGenerator.generate(seed, roundIndex, symbolSet)
        return NextRoundResponse(roundIndex, pattern)
    }

    data class SubmitRequest(
        val roundIndex: Int,
        val attempt: List<String>,   // 사용자가 입력한 시퀀스
        val reactionMs: Int? = null  // 입력에 걸린 시간(선택)
    )
    data class SubmitResult(
        val roundIndex: Int,
        val correct: Boolean,
        val expected: List<String>,
        val finished: Boolean,       // true면 게임 종료(실패 또는 모든 라운드 성공)
        val nextRound: Int?          // 다음 라운드 번호(계속 진행 시)
    )

    /** 라운드 판정 + 메트릭 적재 */
    @Transactional
    fun submit(sessionId: UUID, req: SubmitRequest): SubmitResult {
        val s = sessionRepo.findById(sessionId).orElseThrow()
        val meta = s.meta?.let { M.readValue(it, Map::class.java) } as Map<*, *>
        val seed = (meta["seed"] as Number).toLong()
        val total = (meta["totalRounds"] as Number).toInt()
        val symbolSet = (meta["symbolSet"] as List<*>).map { it.toString() }

        val expected = SequenceGenerator.generate(seed, req.roundIndex, symbolSet)
        val correct = expected == req.attempt

        // 라운드별 메트릭 기록
        metricRepo.save(com.healthcare.play.domain.game.SessionMetric(
            session = s,
            key = if (correct) "round_success" else "round_fail",
            value = req.roundIndex.toDouble(),
            unit = null,
            extra = M.writeValueAsString(mapOf(
                "attempt" to req.attempt,
                "expected" to expected,
                "reactionMs" to (req.reactionMs ?: -1)
            ))
        ))

        // 다음 라운드 진행/종료 판단
        val finished = !correct || req.roundIndex >= total
        val next = if (finished) null else req.roundIndex + 1

        return SubmitResult(
            roundIndex = req.roundIndex,
            correct = correct,
            expected = expected,
            finished = finished,
            nextRound = next
        )
    }

    data class EndPayload(
        val maxLevel: Int,           // 최대로 도달한 라운드 (실패라면 실패 직전 라운드)
        val totalRounds: Int,
        val successCount: Int,
        val failCount: Int,
        val avgInputMs: Int? = null
    )

    /** 세션 종료 및 집계 점수 계산 */
    @Transactional
    fun end(sessionId: UUID, payload: EndPayload): com.healthcare.play.web.dto.SessionSummary {
        // 간단 점수 규칙: 성공 라운드 * 100 + (maxLevel*20)
        val score = payload.successCount * 100 + max(0, payload.maxLevel) * 20
        val accuracy = if (payload.successCount + payload.failCount == 0) null
        else payload.successCount.toDouble() / (payload.successCount + payload.failCount)

        // 집계 메트릭(요약)
        val s = sessionRepo.findById(sessionId).orElseThrow()
        metricRepo.save(com.healthcare.play.domain.game.SessionMetric(
            session = s, key = "summary_success", value = payload.successCount.toDouble()
        ))
        metricRepo.save(com.healthcare.play.domain.game.SessionMetric(
            session = s, key = "summary_fail", value = payload.failCount.toDouble()
        ))
        payload.avgInputMs?.let {
            metricRepo.save(com.healthcare.play.domain.game.SessionMetric(
                session = s, key = "summary_avgInputMs", value = it.toDouble(), unit = "ms"
            ))
        }

        val ended = sessions.end(sessionId, score = score, accuracy = accuracy, durationSec = null,
            meta = mapOf(
                "maxLevel" to payload.maxLevel,
                "totalRounds" to payload.totalRounds,
                "successCount" to payload.successCount,
                "failCount" to payload.failCount,
                "avgInputMs" to (payload.avgInputMs ?: 0)
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