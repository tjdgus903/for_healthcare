package com.healthcare.play.web.controller

import com.healthcare.play.service.AuthService
import com.healthcare.play.service.TokenResponse
import com.healthcare.play.web.dto.LoginRequest
import com.healthcare.play.web.dto.LoginResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val auth: AuthService
) {
    @PostMapping("/signup")
    fun signUp(@RequestBody req: Sign)


    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginRequest
    ): ResponseEntity<TokenResponse> = ResponseEntity.ok(auth.login(req))
}