package com.healthcare.play.web.controller.privacy

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.privacy.ConsentService
import com.healthcare.play.service.privacy.ConsentUpsertReq
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.servlet.http.HttpServletRequest
import java.util.*

@RestController
@RequestMapping("/privacy/consents")
class ConsentController(private val svc: ConsentService) {

    @PostMapping
    fun upsert(@AuthenticationPrincipal p: AuthPrincipal,
               @RequestBody req: ConsentUpsertReq,
               http: HttpServletRequest) {
        val ip = http.getHeader("X-Forwarded-For") ?: http.remoteAddr
        val ua = http.getHeader("User-Agent")
        svc.upsert(p.userId, req, ip, ua)
    }

    @GetMapping
    fun list(@AuthenticationPrincipal p: AuthPrincipal) = svc.history(p.userId)
}