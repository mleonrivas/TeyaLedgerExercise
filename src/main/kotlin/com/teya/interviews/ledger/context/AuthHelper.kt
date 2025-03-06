package com.teya.interviews.ledger.context

import com.teya.interviews.ledger.exceptions.AuthenticationRequiredException
import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.model.UserRole
import org.springframework.security.core.context.SecurityContextHolder

object AuthHelper {
    val USER_ID_HEADER = "X-User-Id"
    val USER_ROLE_HEADER = "X-User-Role"

    fun getAuthenticatedUserId(): String {
        val userId = SecurityContextHolder.getContext().authentication?.principal as String?
        if(userId.isNullOrBlank()) {
            throw AuthenticationRequiredException()
        }
        return userId!!
    }

    fun requireRole(role: UserRole) {
        val r = SecurityContextHolder.getContext().authentication?.authorities?.firstOrNull()?.authority
        if(role.name != r) {
            throw AccessDeniedException()
        }
    }

    fun requireAdmin() {
        requireRole(UserRole.ADMIN)
    }
}