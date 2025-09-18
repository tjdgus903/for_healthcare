package com.healthcare.play.web.dto

import com.fasterxml.jackson.annotation.JsonAlias
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:JsonAlias("id", "username")
    val email: String,

    @field:NotBlank
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)