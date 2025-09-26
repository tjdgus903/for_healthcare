package com.healthcare.play.config

import com.healthcare.play.security.JwtAuthenticationFilter
import com.healthcare.play.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(withDefaults())
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .authorizeHttpRequests { auth ->
                auth
                    // ---------- 공개 리소스 & 페이지 ----------
                    .requestMatchers(
                        "/", "/index", "/index.html",
                        "/login", "/login.html",
                        "/favicon.ico",
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/service.js/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/h2-console/**",
                        "/actuator/health",
                        "/ping",
                        // 템플릿 '페이지' 라우트는 열어둠 (페이지는 열리지만, 내부 API는 별도 인증)
                        "/games", "/attendance", "/reports", "/subs"
                    ).permitAll()

                    // ---------- 인증/발급 관련 공개 엔드포인트 ----------
                    .requestMatchers("/auth/**").permitAll()

                    // ---------- 보호할 API (여기 중요!) ----------
                    // 페이지(/attendance)는 permitAll이지만, 하위 API(/attendance/**)는 인증 필요
                    .requestMatchers(
                        "/attendance/**",    // 예: /attendance/calendar, /attendance/check
                        "/sessions/**",      // 예: /sessions/start, /sessions/{id}/end
                        "/reports/**",       // 예: /reports/me
                        "/privacy/**",       // 예: /privacy/consents, /privacy/export.zip
                        "/subs/**",          // 예: /subs/status, /subs/verify
                        "/ads/**"            // 예: /ads/config (필요시 조정)
                    ).authenticated()

                    // ---------- 그 외 ----------
                    .anyRequest().permitAll() // 새 페이지/리소스는 기본 공개(필요 시 .authenticated()로 변경)
            }

            // H2 콘솔 프레임 허용(개발용)
            .headers { it.frameOptions { fo -> fo.disable() } }

            // JWT 필터 삽입 (UsernamePasswordAuthenticationFilter 앞)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}