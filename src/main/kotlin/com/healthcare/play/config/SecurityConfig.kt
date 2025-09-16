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
@EnableConfigurationProperties(JwtProperties::class) // 또는 메인에 @ConfigurationPropertiesScan
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .csrf { it.disable() }
            .cors(withDefaults())

            .authorizeHttpRequests { auth ->
                auth
                    // 정적 리소스 & 루트 허용 (index.html 볼 수 있게!)
                    .requestMatchers("/", "/index.html", "/favicon.ico",
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/ping", "/games/**", "/dev/**" ).permitAll()
                    // 공개 엔드포인트
                    .requestMatchers("/auth/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/h2-console/**",
                        "/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/").permitAll()
                    // 그 외는 인증 필요
                    .anyRequest().authenticated()
            }

            // H2 콘솔 프레임 허용(로컬)
            .headers { it.frameOptions { fo -> fo.disable() } }

            // JWT 필터 삽입
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}