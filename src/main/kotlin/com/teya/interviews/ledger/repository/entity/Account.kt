package com.teya.interviews.ledger.repository.entity

import com.teya.interviews.ledger.model.AccountStatus
import com.teya.interviews.ledger.model.Currency
import jakarta.persistence.*
import java.math.BigDecimal

/**
 * Persistence representation of the Account.
 */
@Entity
@Table(
    name = "accounts",
    indexes = [
        Index(name = "owner", columnList = "owner_id"),
    ]
)
data class Account (
    // TODO: add account type (CHECKING, CREDIT_CARD, SAVINGS...)?
    // TODO: add authorized users?

    // Immutable properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    var id: Long? = null,

    @Column(nullable = false)
    var currency: Currency,

    @ManyToOne(optional = true, targetEntity = User::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    var owner: User,

    // Mutable properties:
    @Column(nullable = false)
    var status: AccountStatus,

    @Column(nullable = true)
    var alias: String? = null,

    // TODO: Check if performance is a concern, if so, use Int that represents cents: 5.99 -> 599.
    @Column(nullable = false)
    var balance: BigDecimal,

    // Used for optimistic locking
    @Version
    var version: Long? = null
)