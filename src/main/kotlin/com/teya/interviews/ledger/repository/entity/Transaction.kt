package com.teya.interviews.ledger.repository.entity

import com.teya.interviews.ledger.model.Currency
import com.teya.interviews.ledger.model.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "transactions")
data class Transaction (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    var id: Long? = null,

    @ManyToOne(optional = true, targetEntity = Account::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "source_account_id")
    val sourceAccount: Account? = null,

    @ManyToOne(optional = false, targetEntity = Account::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_account_id")
    val destinationAccount: Account,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = true)
    val concept: String?,

    // assume transactions are immediate and do not need a completion timestamp
    @Column(nullable = false)
    val timestamp: Instant,

    @Column(nullable = true)
    val originCurrency: Currency? = null,
    @Column(nullable = false)
    val destinationCurrency: Currency,
    @Column(nullable = true)
    val appliedConversionRate: BigDecimal? = null,
    @Column(nullable = true)
    val convertedAmount: BigDecimal? = null
)