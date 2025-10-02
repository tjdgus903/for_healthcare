package com.healthcare.play.config

import com.healthcare.play.security.JwtAuthenticationFilter
import com.healthcare.play.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
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
            .cors { } // 기본 CORS 허용(필요시 커스터마이징)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            .authorizeHttpRequests { auth ->
                auth
                    // 1) 프리플라이트(브라우저 OPTIONS) 허용
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 2) 정적 리소스/공개 페이지
                    .requestMatchers(
                        "/", "/index", "/index.html",
                        "/login", "/login.html",
                        "/favicon.ico",
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/service.js/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/h2-console/**",
                        "/actuator/health",
                        "/ping",
                        // 정적 파셜 (static/games/**)
                        "/games/**", "/attendance/**", "/reports/**", "/subs/**"
                    ).permitAll()

                    // 3) 인증/발급 관련
                    .requestMatchers("/auth/**").permitAll()

                    // 4) 인증이 필요한 API (여기 중요!)
                    .requestMatchers(
                        "/sessions/**",
                        "/attendance/**",
                        "/reports/**",
                        "/privacy/**",
                        "/subs/**",
                        "/ads/**"
                    ).authenticated()

                    // 5) 그 외는 페이지로 간주 → 공개
                    .anyRequest().permitAll()
            }

            // H2 콘솔(개발용)
            .headers { it.frameOptions { fo -> fo.disable() } }

            // JWT 필터 삽입 (UsernamePasswordAuthenticationFilter 앞)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
