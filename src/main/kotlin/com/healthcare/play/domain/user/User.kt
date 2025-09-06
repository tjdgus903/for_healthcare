package com.healthcare.play.domain.user

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import org.hibernate.annotations.UuidGenerator
import java.util.*

enum class UserRole { PLAYER, CAREFIVER, ADMIN}

@Entity @Table(name = "users")
class User (
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.PLAYER
)