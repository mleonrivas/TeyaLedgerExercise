package com.teya.interviews.ledger.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Teya Ledger API")
                    .version("v1.0")
            )
            .addSecurityItem(SecurityRequirement().addList("X-User-Id").addList("X-User-Role"))
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("X-User-Id", customHeaderScheme("X-User-Id"))
                    .addSecuritySchemes("X-User-Role", customHeaderScheme("X-User-Role"))
            )
    }

    private fun customHeaderScheme(name: String): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .`in`(SecurityScheme.In.HEADER)
            .name(name)
    }
}