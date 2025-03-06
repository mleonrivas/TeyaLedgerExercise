package com.teya.interviews.ledger.config

import com.teya.interviews.ledger.context.AuthContextFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class AuthConfig (private val authContextFilter: AuthContextFilter) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // allow swagger to be accessed without authentication
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated() // everything else is under authentication
            }
            .httpBasic { it.disable() }  // Disables basic auth
            .formLogin { it.disable() }  // Disables form login
            .addFilterBefore(authContextFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}