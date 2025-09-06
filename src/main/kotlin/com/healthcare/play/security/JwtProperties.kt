package com.healthcare.play.security

import org.springframework.boot.context.properties.ConfigurationProperties

// application.yml의 jwt.* 값을 바인딩
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val issuer: String,
    val accessTokenMinutes: Long
)