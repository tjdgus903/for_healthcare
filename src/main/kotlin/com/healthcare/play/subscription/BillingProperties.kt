package com.healthcare.play.subscription

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "billing")
data class BillingProperties(
    val devAccept: Boolean = false,
    val defaultDays: Long = 30
)