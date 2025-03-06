package com.teya.interviews.ledger.model.dto

// DTO Transformer requires a default (no params) constructor, that's why all properties are nullable
data class UserDTO(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null
)