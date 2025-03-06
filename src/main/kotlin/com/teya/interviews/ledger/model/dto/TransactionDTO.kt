package com.teya.interviews.ledger.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teya.interviews.ledger.model.Currency
import com.teya.interviews.ledger.model.TransactionType
import java.math.BigDecimal
import java.time.Instant

// DTO Transformer requires a default (no params) constructor, that's why all properties are nullable
data class TransactionDTO(
    var id: Long? = null,
    var sourceAccountId: Long? = null,
    var destinationAccountId: Long? = null,
    var type: TransactionType? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var amount: BigDecimal? = null,
    var concept: String? = null,
    var timestamp: Instant? = null,
    var originCurrency: Currency? = null,
    var destinationCurrency: Currency? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var appliedConversionRate: BigDecimal? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var convertedAmount: BigDecimal? = null
)