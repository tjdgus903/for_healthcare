package com.healthcare.play.web.controller

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.SequenceMemoryService
import com.healthcare.play.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/sequence")
class SequenceMemoryController(
    private val svc: SequenceMemoryService
) {
    /** 세션 시작 + 1라운드 패턴 반환 */
    @PostMapping("/start")
    fun start(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody @Valid req: SequenceStartRequest
    ): ResponseEntity<SequenceStartResponse> {
        val r = svc.start(principal.userId,
            SequenceMemoryService.StartOptions(
                totalRounds = req.totalRounds,
                symbolSet = req.symbolSet,
                showMs = req.showMs,
                inputTimeoutMs = req.inputTimeoutMs
            )
        )
        return ResponseEntity.ok(
            SequenceStartResponse(
                sessionId = r.sessionId, seed = r.seed, totalRounds = r.totalRounds,
                symbolSet = r.symbolSet, roundIndex = r.roundIndex, pattern = r.pattern,
                showMs = r.showMs, inputTimeoutMs = r.inputTimeoutMs
            )
        )
    }

    /** (선택) 특정 라운드 패턴 재조회 */
    @GetMapping("/{sessionId}/pattern")
    fun pattern(
        @PathVariable sessionId: UUID,
        @RequestParam roundIndex: Int
    ): SequenceGetPatternResponse {
        val r = svc.getPattern(sessionId, roundIndex)
        return SequenceGetPatternResponse(r.roundIndex, r.pattern)
    }

    /** 라운드 제출(채점) */
    @PostMapping("/{sessionId}/submit")
    fun submit(
        @PathVariable sessionId: UUID,
        @RequestBody @Valid req: SequenceSubmitRequest
    ): SequenceSubmitResponse {
        val r = svc.submit(sessionId, SequenceMemoryService.SubmitRequest(
            roundIndex = req.roundIndex,
            attempt = req.attempt,
            reactionMs = req.reactionMs
        ))
        return SequenceSubmitResponse(r.roundIndex, r.correct, r.expected, r.finished, r.nextRound)
    }

    /** 세션 종료(집계) */
    @PostMapping("/{sessionId}/end")
    fun end(
        @PathVariable sessionId: UUID,
        @RequestBody @Valid req: SequenceEndRequest
    ): com.healthcare.play.web.dto.SessionSummary {
        return svc.end(sessionId, SequenceMemoryService.EndPayload(
            maxLevel = req.maxLevel,
            totalRounds = req.totalRounds,
            successCount = req.successCount,
            failCount = req.failCount,
            avgInputMs = req.avgInputMs
        ))
    }
}
