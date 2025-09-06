package com.healthcare.play.web.dto

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties.Token

data class LoginRequest(
    @field:NotBlank val id: String,         // = email 필드(현재 fhc 사용)
    @field:NotBlank val password: String,   // = fhc
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)