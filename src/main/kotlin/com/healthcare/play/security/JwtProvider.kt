package com.healthcare.play.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.crypto.spec.SecretKeySpec

data class JwtPayload(
    val userId: UUID,
    val email: String,
    val role: String
)

@Component
class JwtProvider(
    private val props: JwtProperties,
) {
    // private val key = Keys.hmacShaKeyFor(props.secret.toByteArray(StandardCharsets.UTF_8))
    private val key = SecretKeySpec(
        Base64.getDecoder().decode(props.secret),
        SignatureAlgorithm.HS256.jcaName
    )

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

    fun createToken(userId: UUID, role: String): String {
        val now = Date()
        val expiry = Date(now.time + java.time.Duration.ofMinutes(props.accessTokenMinutes).toMillis())

        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }
}