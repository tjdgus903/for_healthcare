package com.healthcare.play.security

import org.apache.el.parser.AstMinus
import org.springframework.boot.context.properties.bind.ConstructorBinding

data class JwtProperties @ConstructorBinding constructor(
    val secret: String,
    val issuer: String,
    val accessTokenMinutes: Long
)