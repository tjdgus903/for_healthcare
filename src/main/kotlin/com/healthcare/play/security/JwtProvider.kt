package com.healthcare.play.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

data class JwtPayload(
    val userId: UUID,
    val email: String,
    val role: String
)

@Component
class JwtProvider(
    private val props: JwtProperties,
) {
    private val key = Keys.hmacShaKeyFor(props.secret.toByteArray(StandardCharsets.UTF_8))

    fun generateAccessToken(userId: UUID, email: String, role: String): String{
        val now = Instant.now()
        val exp = now.plusSeconds(props.accessTokenMinutes * 60)
        return Jwts.builder()
            .setIssuer(props.issuer)
            .setSubject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): JwtPayload {
        val jws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        val body = jws.body
        return JwtPayload(
            userId = UUID.fromString(body.subject),
            email = body["email"] as String,
            role = body["role"] as String
        )
    }
}