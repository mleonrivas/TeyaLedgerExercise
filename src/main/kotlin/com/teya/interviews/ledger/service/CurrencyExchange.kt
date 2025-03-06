package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyExchange {
    // Rates are hardcoded for simplicity, ideally this should be a service that fetches rates externally
    private val rates = mapOf(
        Currency.USD to BigDecimal("1.0000"),
        Currency.EUR to BigDecimal("0.9500"),
        Currency.GBP to BigDecimal("0.7900")
    )

    fun convert(amount: BigDecimal, from: Currency, to: Currency): Conversion {
        val rate = rates[to]!!.div(rates[from]!!).setScale(4, RoundingMode.DOWN)
        // All currencies supported have 2 decimal places
        val result = amount.times(rate)
        return Conversion(result.setScale(2, RoundingMode.DOWN), rate)
    }
}

data class Conversion(
    val result: BigDecimal,
    val appliedRate: BigDecimal
)