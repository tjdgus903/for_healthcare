package com.healthcare.play.service

import com.healthcare.play.domain.user.UserRepository.UserRepository
import com.healthcare.play.security.JwtProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


/**
 * 로그인 검증 : id/pw 확인 -> JWT 발급.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    fun login(id: String, password: String): String {
        val user = userRepository.findByEmail(id)
            .orElseThrow { IllegalArgumentException("Invalid credentials") }

        if (!passwordEncoder.matches(password, user.passwordHash))
            throw IllegalArgumentException("Invalid credentials")

        return jwtProvider.generateAccessToken(
            userId = user.id ?: throw IllegalStateException("User id missing"),
            email = user.email,
            role = user.role.name
        )
    }
}