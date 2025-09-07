package com.healthcare.play.web.controller

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.ShapeMatchService
import com.healthcare.play.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/shape")
class ShapeMatchController(
    private val svc: ShapeMatchService
) {
    @PostMapping("/start")
    fun start(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody @Valid req: ShapeStartRequest
    ): ResponseEntity<ShapeStartResponse> {
        val r = svc.start(principal.userId,
            ShapeMatchService.StartOptions(
                rows = req.rows, cols = req.cols, totalRounds = req.totalRounds,
                shapeSet = req.shapeSet, inputTimeoutMs = req.inputTimeoutMs
            )
        )
        return ResponseEntity.ok(
            ShapeStartResponse(
                sessionId = r.sessionId, seed = r.seed, rows = r.rows, cols = r.cols,
                totalRounds = r.totalRounds, shapeSet = r.shapeSet,
                roundIndex = r.roundIndex, target = r.target, cells = r.cells,
                inputTimeoutMs = r.inputTimeoutMs
            )
        )
    }

    @GetMapping("/{sessionId}/layout")
    fun layout(@PathVariable sessionId: UUID, @RequestParam roundIndex: Int): ShapeLayoutResponse {
        val r = svc.getLayout(sessionId, roundIndex)
        return ShapeLayoutResponse(r.roundIndex, r.target, r.cells)
    }

    @PostMapping("/{sessionId}/submit")
    fun submit(@PathVariable sessionId: UUID, @RequestBody @Valid req: ShapeSubmitRequest): ShapeSubmitResponse {
        val r = svc.submit(sessionId, ShapeMatchService.SubmitRequest(
            roundIndex = req.roundIndex, chosenIndex = req.chosenIndex, reactionMs = req.reactionMs
        ))
        return ShapeSubmitResponse(r.roundIndex, r.correct, r.targetIndex, r.finished, r.nextRound)
    }

    @PostMapping("/{sessionId}/end")
    fun end(@PathVariable sessionId: UUID, @RequestBody @Valid req: ShapeEndRequest)
            = svc.end(sessionId, ShapeMatchService.EndPayload(
        totalRounds = req.totalRounds,
        hitCount = req.hitCount,
        missCount = req.missCount,
        avgReactionMs = req.avgReactionMs
    ))

    @PostMapping("/{sessionId}/extra-time-used")
    fun extraTimeUsed(@PathVariable sessionId: UUID,
                      @RequestParam(required=false, defaultValue="0") roundIndex: Int = 0) {
        // 동일하게 메트릭 저장 로직(선택)
    }
}
