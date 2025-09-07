package com.healthcare.play.web.controller.privacy

import com.healthcare.play.security.AuthPrincipal
import com.healthcare.play.service.privacy.DataEraseService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/privacy")
class EraseController(private val svc: DataEraseService) {

    @PostMapping("/erase")
    fun erase(@AuthenticationPrincipal p: AuthPrincipal) {
        svc.requestErase(p.userId)
    }
}