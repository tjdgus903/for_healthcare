package com.healthcare.play.web.controller

import com.healthcare.play.domain.user.UserRole
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
    fun signUp(@RequestBody req: SignUpRequset) =
        ResponseEntity.ok().build<Unit>().also {
            println(req)
            auth.signUp(req.email, req.password, UserRole.PLAYER)
        }

    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginRequest
    ): ResponseEntity<TokenResponse> = ResponseEntity.ok(auth.login(req))
}

data class SignUpRequset(val email: String, val password: String)