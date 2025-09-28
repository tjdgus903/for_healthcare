package com.healthcare.play.web.controller

import com.healthcare.play.domain.game.GameType
import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.GameCatalogService
import com.healthcare.play.service.GameSessionService
import com.healthcare.play.service.MetricItem
import com.healthcare.play.web.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/game")
class GameControllers (
    private val catelog: GameCatalogService
){
    fun list(): List<GameSimple> =
        catelog.listActive().map { GameSimple(it.id!!, it.type, it.name, it.description)}
}


@RestController
@RequestMapping("/sessions")
class SessionController(
    private val sessions: GameSessionService
) {
    /** 세션 시작 */
    @PostMapping("/start")
    fun start(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestBody @Valid req: StartSessionRequest
    ): ResponseEntity<StartSessionResponse> {
        val s = sessions.start(principal.userId, req.gameType, req.meta)
        return ResponseEntity.ok(StartSessionResponse(s.id!!, s.game.type, s.startedAt))
    }

    /** 세션 지표 추가(여러 번 호출 가능) */
    @PostMapping("/{sessionId}/metrics")
    fun addMetrics(
        @PathVariable sessionId: UUID,
        @RequestBody req: AddMetricsRequest
    ) {
        sessions.addMetrics(sessionId, req.metrics.map { MetricItem(it.key, it.value, it.unit, it.extra) })
    }

    /** 세션 종료 */
    @PostMapping("/{sessionId}/end")
    fun end(
        @PathVariable sessionId: UUID,
        @RequestBody req: EndSessionRequest
    ): SessionSummary {
        val s = sessions.end(sessionId, req.score, req.accuracy, req.durationSec, req.meta)
        return SessionSummary(s.id!!, s.game.type, s.startedAt, s.endedAt, s.durationSec, s.score, s.accuracy)
    }

    /** 내 세션 리스트(옵션: gameType 필터) */
    @GetMapping("/me")
    fun mySessions(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam(required = false) gameType: GameType?
    ): List<SessionSummary> =
        sessions.listMySessions(principal.userId, gameType).map {
            SessionSummary(it.id!!, it.game.type, it.startedAt, it.endedAt, it.durationSec, it.score, it.accuracy)
        }
}