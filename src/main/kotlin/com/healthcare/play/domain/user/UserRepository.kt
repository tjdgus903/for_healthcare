package com.healthcare.play.domain.user.UserRepository

import com.healthcare.play.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID>{
    fun findByEmail(email: String): Optional<User>
}