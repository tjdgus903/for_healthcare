package com.healthcare.play.orgs

import com.healthcare.play.security.AuthPrincipal
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/orgs")
class OrgReportController(
    private val svc: OrgReportService
) {
    @GetMapping("/{orgId}/reports/summary")
    fun summary(
        @AuthenticationPrincipal p: AuthPrincipal,
        @PathVariable orgId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant,
        @RequestParam(required=false) cohortId: UUID?
    ): OrgSummaryResponse = svc.summaryByGame(orgId, p.userId, from, to, cohortId)

    @GetMapping("/{orgId}/reports/export.csv")
    fun exportCsv(
        @AuthenticationPrincipal p: AuthPrincipal,
        @PathVariable orgId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant,
        @RequestParam(required=false) cohortId: UUID?
    ): ResponseEntity<ByteArray> {
        val data = svc.summaryByGame(orgId, p.userId, from, to, cohortId)
        val sb = StringBuilder()
        sb.appendLine("gameCode,sessions,avgScore,avgAccuracy,totalDurationSec")
        data.rows.forEach {
            sb.append(it.gameLabel).append(',')
               .append(it.sessions).append(',')
               .append(it.avgScore ?: "").append(',')
               .append(it.avgAccuracy ?: "").append(',')
               .append(it.totalDurationSec).appendLine()
        }
        val bytes = sb.toString().toByteArray(StandardCharsets.UTF_8)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"org-${orgId}-summary.csv\"")
            .contentType(MediaType("text","csv"))
            .body(bytes)
    }

    @GetMapping("/{orgId}/reports/export.pdf")
    fun exportPdf(
        @AuthenticationPrincipal p: AuthPrincipal,
        @PathVariable orgId: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant,
        @RequestParam(required=false) cohortId: UUID?
    ): ResponseEntity<ByteArray> {
        val data = svc.summaryByGame(orgId, p.userId, from, to, cohortId)
        val bytes = ReportPdf.renderSummary(data)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"org-${orgId}-summary.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(bytes)
    }
}