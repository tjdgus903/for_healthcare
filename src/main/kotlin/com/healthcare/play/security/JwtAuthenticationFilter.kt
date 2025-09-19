package com.healthcare.play.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        var header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer")){
            val token = header.removePrefix("Bearer").trim()
            try{
                val payload = jwtProvider.parse(token)
                val principal = AuthPrincipal(
                    userId = payload.userId,
                    email = payload.email,
                    role = payload.role,
                )
                val auth = UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${payload.role}"))
                )
                SecurityContextHolder.getContext().authentication = auth
            }catch (e: Exception){
                // 토큰 파싱 실패 시 그냥 통과 (컨트롤러 단에서 401/403)
                SecurityContextHolder.clearContext()
            }
        }
        filterChain.doFilter(request, response)
    }
}