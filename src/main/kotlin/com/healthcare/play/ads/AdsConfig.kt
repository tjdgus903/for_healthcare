package com.healthcare.play.ads

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AdsProperties::class)
class AdsConfig
