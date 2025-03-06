package com.teya.interviews.ledger.repository.entity

import com.teya.interviews.ledger.model.LedgerEntryType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * A representation of each operation that affects the account balance.
 */
@Entity
@Table(
    name = "ledger",
    indexes = [
        Index(name = "idx_account_id", columnList = "account_id"),
        Index(name = "idx_timestamp", columnList = "timestamp")
    ]
)
data class LedgerEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    var id: Long? = null,

    @ManyToOne(optional = false, targetEntity = Account::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    val account: Account,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    val type: LedgerEntryType,

    @Column(nullable = true)
    val concept: String?,

    @Column(nullable = false)
    val timestamp: Instant,

    @ManyToOne(optional = true, targetEntity = Transaction::class, fetch = FetchType.EAGER)
    @JoinColumn(name="transaction_id")
    val transaction: Transaction? = null
)
