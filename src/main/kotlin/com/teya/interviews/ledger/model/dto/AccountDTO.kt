package com.teya.interviews.ledger.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.teya.interviews.ledger.model.AccountStatus
import com.teya.interviews.ledger.model.Currency
import java.math.BigDecimal

// DTO Transformer requires a default (no params) constructor, that's why all properties are nullable
data class AccountDTO(
    var id: Long? = null,
    var currency: Currency? = null,
    var status: AccountStatus? = null,
    var alias: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    var balance: BigDecimal? = null,
)
