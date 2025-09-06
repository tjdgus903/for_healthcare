package com.healthcare.play.web.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank val id: String,
    @field:NotBlank val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)