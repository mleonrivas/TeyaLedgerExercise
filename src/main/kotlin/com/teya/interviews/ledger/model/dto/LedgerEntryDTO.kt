package com.teya.interviews.ledger.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teya.interviews.ledger.model.LedgerEntryType
import java.math.BigDecimal
import java.time.Instant

// DTO Transformer requires a default (no params) constructor, that's why all properties are nullable
data class LedgerEntryDTO (
    var id: Long? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var amount: BigDecimal? = null,
    var type: LedgerEntryType? = null,
    var concept: String? = null,
    var timestamp: Instant? = null,
    var transactionId: Long? = null
)