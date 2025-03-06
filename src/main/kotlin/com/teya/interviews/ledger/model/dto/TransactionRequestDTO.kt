package com.teya.interviews.ledger.model.dto

import com.teya.interviews.ledger.model.TransactionType
import java.math.BigDecimal

data class TransactionRequestDTO(
    val type: TransactionType,
    val amount: BigDecimal,
    val accountId: Long,
    val toAccountId: Long? = null,
    val concept: String? = null
)