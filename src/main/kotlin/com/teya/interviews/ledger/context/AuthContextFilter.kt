package com.teya.interviews.ledger.context

import com.teya.interviews.ledger.model.UserRole
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthContextFilter : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest) {
            val userId = request.getHeader("X-User-Id")
            var authData = UsernamePasswordAuthenticationToken.unauthenticated(null, null)
            if(!userId.isNullOrBlank()) {
                val userRole = request.getHeader("X-User-Role")
                val role = if(userRole.isNullOrBlank()) UserRole.USER else UserRole.valueOf(userRole)
                authData = UsernamePasswordAuthenticationToken.authenticated(userId, null, listOf(SimpleGrantedAuthority(role.name)))
            }

            SecurityContextHolder.getContext().authentication = authData
        }

        chain.doFilter(request, response)

    }
}