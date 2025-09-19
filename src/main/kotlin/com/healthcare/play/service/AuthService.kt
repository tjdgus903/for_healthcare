package com.healthcare.play.service

import com.healthcare.play.domain.user.User
import com.healthcare.play.domain.user.UserRepository
import com.healthcare.play.domain.user.UserRole
import com.healthcare.play.security.JwtProvider
import com.healthcare.play.web.dto.LoginRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant


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

        val token = jwtProvider.generateAccessToken(user.id!!, user.email, user.role.name)
        return TokenResponse(token)
    }

    @Transactional
    fun signUp(email: String, rawPassword: String, role: UserRole = UserRole.PLAYER){
        require(!userRepo.existsByEmail(email)) { "Already exists: $email" }
        val user = User(
            email = email,
            passwordHash = passwordEncoder.encode(rawPassword),
            role = role,
            createdAt = Instant.now()
        )
        userRepo.save(user)
    }
}

data class TokenResponse(val token: String)