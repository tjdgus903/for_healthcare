package com.healthcare.play.config

import com.healthcare.play.security.JwtAuthFilter
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
    private val jwtAuthFilter: JwtAuthFilter
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // REST API 스타일: 세션은 사용하지 않음
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors(withDefaults())

            // URL 접근 권한
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/h2-console/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                    .anyRequest().authenticated()
            }

            // H2 콘솔 프레임 허용 (로컬 용)
            .headers { it.frameOptions { fo -> fo.disable() } }

            // UsernamePasswordAuthenticationFilter 앞에 JWT 필터 삽입
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}