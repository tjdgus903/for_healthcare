package com.healthcare.play.config

import com.healthcare.play.domain.user.User
import com.healthcare.play.domain.user.UserRepository.UserRepository
import com.healthcare.play.domain.user.UserRole
import com.healthcare.play.service.GameCatalogService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Profile("local")
class DevDataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val gameCatalog: GameCatalogService,
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

        if (userRepository.findByEmail("fhc2").isEmpty) {
            userRepository.save(
                User(
                    email = "fhc2",
                    passwordHash = passwordEncoder.encode("fhc2"),
                    role = UserRole.PLAYER
                )
            )
            println("▶ Demo user created: fhc2/fhc2")
        }

        // 게임 카탈로그 기본값 보장
        gameCatalog.ensureDefaults()
        println("▶ Default games ensured.")
    }
}