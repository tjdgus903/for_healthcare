package com.healthcare.play.service

import com.healthcare.play.domain.user.UserRepository
import com.healthcare.play.security.JwtProvider
import com.healthcare.play.web.dto.LoginRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


/**
 * 로그인 검증 : id/pw 확인 -> JWT 발급.
 */
@Service
class AuthService(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    fun login(req: LoginRequest): TokenResponse {
        val user = userRepo.findByEmail(req.email).orElseThrow {
            IllegalArgumentException("No user for email=${req.email}")
        }
        require(passwordEncoder.matches(req.password, user.passwordHash)) { "Bad credentials" }

        val token = jwtProvider.createToken(user.id!!, user.role.name)
        return TokenResponse(token)
    }
}

data class TokenResponse(val token: String)