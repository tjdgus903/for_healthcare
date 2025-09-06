package com.healthcare.play.web.controller

import com.healthcare.play.service.AuthService
import com.healthcare.play.web.dto.LoginRequest
import com.healthcare.play.web.dto.LoginResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController (
    private val authService: AuthService
){
    /**
     * 로그인 -> JWT 발급
     * curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d "{\"id\":\"fhc\",\"password\":\"fhc\"}"
     */
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<LoginResponse>{
        val token = authService.login(req.id, req.password)
        return ResponseEntity.ok(LoginResponse(accessToken = token))
    }
}