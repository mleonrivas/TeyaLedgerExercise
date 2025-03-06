package com.teya.interviews.ledger.model.dto

import com.teya.interviews.ledger.model.Currency
import java.math.BigDecimal

data class CreateAccountRequestDTO(
    var currency: Currency ?= null,
    var initialBalance: BigDecimal? = null,
    var ownerId: String ? = null,
    var alias: String? = null
)