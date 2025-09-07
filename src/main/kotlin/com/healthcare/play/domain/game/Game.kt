package com.healthcare.play.domain.game

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.UuidGenerator
import org.springframework.context.annotation.Description
import java.util.*

@Entity
@Table(name = "game")
class Game (
    @Id @GeneratedValue @UuidGenerator
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    var type: GameType,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(nullable = false, length = 500)
    var description: String = "",

    @Column(nullable = false)
    var active: Boolean = true,
)