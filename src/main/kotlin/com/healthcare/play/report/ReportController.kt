package com.healthcare.play.report

import com.healthcare.play.security.AuthPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/reports")
class ReportController(
    private val reportService: ReportService
) {

    /**
     * 내 리포트(기본 최근 30일)
     * GET /report/me?days=30
     */
    @GetMapping("/me")
    fun myReport(
        @AuthenticationPrincipal principal: AuthPrincipal,
        @RequestParam("days", required = false, defaultValue = "30") days: Int,
    ): ResponseEntity<MyReportResponse>{
        val body = reportService.myReport(principal.userId, days)
        return ResponseEntity.ok(body)
    }
}