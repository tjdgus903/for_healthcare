package com.healthcare.play.web.controller.privacy

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.privacy.DataExportService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping("/privacy")
class DataPortabilityController(private val exportSvc: DataExportService) {

    @GetMapping("/export.zip")
    fun export(@AuthenticationPrincipal p: AuthPrincipal): ResponseEntity<ByteArray> {
        val bytes = exportSvc.exportUser(p.userId)
        val name = "export-${p.userId}-${Instant.now().toEpochMilli()}.zip"
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$name\"")
            .contentType(MediaType("application","zip"))
            .body(bytes)
    }
}