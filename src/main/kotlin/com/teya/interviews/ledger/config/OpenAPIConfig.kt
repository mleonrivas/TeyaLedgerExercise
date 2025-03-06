package com.teya.interviews.ledger.config

import com.teya.interviews.ledger.context.AuthHelper
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
            .addSecurityItem(SecurityRequirement().addList(AuthHelper.USER_ID_HEADER).addList(AuthHelper.USER_ROLE_HEADER))
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes(AuthHelper.USER_ID_HEADER, customHeaderScheme(AuthHelper.USER_ID_HEADER))
                    .addSecuritySchemes(AuthHelper.USER_ROLE_HEADER, customHeaderScheme(AuthHelper.USER_ROLE_HEADER))
            )
    }

    private fun customHeaderScheme(name: String): SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .`in`(SecurityScheme.In.HEADER)
            .name(name)
    }
}