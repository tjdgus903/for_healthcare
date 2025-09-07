package com.healthcare.play.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerOpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val bearer = "bearerAuth"
        return OpenAPI()
            .info(Info().title("Healthcare API").version("v1"))
            .components(
                Components().addSecuritySchemes(
                    bearer,
                    SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(bearer))
    }
}