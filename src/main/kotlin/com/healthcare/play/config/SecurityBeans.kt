package com.healthcare.play.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class SecurityBeans {
    // @SpringBootApplication 의 위치가 상위에 있으면 자동으로 스캔에 잡힘
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}