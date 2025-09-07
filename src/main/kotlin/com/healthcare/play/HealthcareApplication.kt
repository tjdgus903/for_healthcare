package com.healthcare.play

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class HealthcareApplication

fun main(args: Array<String>) {
    runApplication<HealthcareApplication>(*args)
}