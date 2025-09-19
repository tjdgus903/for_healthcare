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
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors(withDefaults())

            .authorizeHttpRequests {
                // 정적 리소스 & 루트 허용
                it.requestMatchers("/", "/index.html", "/favicon.ico",
                    "/assets/**", "/css/**", "/service.js/**", "/images/**", "/webjars/**",
                    "/ping", "/games/**", "/dev/**" ).permitAll()

                // 공개 엔드포인트
                it.requestMatchers("/auth/**",
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/h2-console/**",
                    "/actuator/health").permitAll()

                // 출석 API는 인증 필요 (단, GET 달력 조회만 공개라면 아래처럼 분리)
                // it.requestMatchers(HttpMethod.GET, "/attendance/calendar").permitAll()
                it.requestMatchers("/attendance/**").authenticated()

                // 그 외는 인증 필요
                it.anyRequest().authenticated()
            }

            // H2 콘솔 프레임 허용
            .headers { it.frameOptions { fo -> fo.disable() } }

            // JWT 필터 삽입
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
