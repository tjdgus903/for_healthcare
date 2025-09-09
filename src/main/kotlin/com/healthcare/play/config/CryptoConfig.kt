package com.healthcare.play.config

import com.healthcare.play.security.crypto.CryptoProps
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CryptoProps::class)
class CryptoConfig