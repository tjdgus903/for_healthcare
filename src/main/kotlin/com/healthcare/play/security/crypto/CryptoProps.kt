package com.healthcare.play.security.crypto

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("crypto")
data class CryptoProps(
    val aesKeyBase64: String // 256-bit base64키
)