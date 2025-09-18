package com.healthcare.play.subscription

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "billing")
data class BillingProperties(
    var devAccept: Boolean = true,
    var defaultDays: Long = 7
)
