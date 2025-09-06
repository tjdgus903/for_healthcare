package com.healthcare.play.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 매 요청마다 Authorization: Bearer <token> 을 파싱/검증하고
 * SecurityContext에 인증 객체를 심는다.
 */
@Component
class JwtAuthFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)
        if (token != null) {
            try {
                val payload = jwtProvider.parse(token)
                val auth = UsernamePasswordAuthenticationToken(
                    // principal: 우리가 쓸 사용자 식별 정보
                    AuthPrincipal(payload.userId, payload.email, payload.role),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${payload.role}"))
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (ex: Exception) {
                // 토큰 오류 시 인증 미설정 상태로 통과 -> 이후 권한필터에서 401/403
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!StringUtils.hasText(header)) return null
        if (!header.startsWith("Bearer ")) return null
        return header.substring("Bearer ".length)
    }
}

/** 컨트롤러에서 @AuthenticationPrincipal 대용으로 쓸 간단한 Principal */
data class AuthPrincipal(
    val userId: java.util.UUID,
    val email: String,
    val role: String
)