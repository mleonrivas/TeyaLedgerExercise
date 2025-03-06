package com.teya.interviews.ledger.model.dto

import com.teya.interviews.ledger.model.UserRole

data class CreateUserRequestDTO(
    val name: String,
    val email: String?,
    val phone: String?,
    val role: UserRole = UserRole.USER
)
