package com.teya.interviews.ledger.repository.entity

import com.teya.interviews.ledger.model.UserRole
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @Column(nullable = false, unique = true)
    val id: String,
    @Column(nullable = false)
    var name: String? = null,
    @Column
    var email: String? = null,
    @Column
    var phone: String? = null,
    @Column
    var role: UserRole = UserRole.USER
)
