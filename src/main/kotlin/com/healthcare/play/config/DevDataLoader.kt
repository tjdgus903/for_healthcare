package com.healthcare.play.config

import org.example.com.healthcare.play.domain.user.User
import org.example.com.healthcare.play.domain.user.UserRepository
import org.example.com.healthcare.play.domain.user.UserRole
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Profile("local")
class DevDataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder   // ← 여기 주입
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val demo = User(
                email = "fhc",
                passwordHash = passwordEncoder.encode("fhc"),
                role = UserRole.PLAYER
            )
            userRepository.save(demo)
            println("▶ Demo user created: id=${demo.id}, email=${demo.email}")
        }
    }
}