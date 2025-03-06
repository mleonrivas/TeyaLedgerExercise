package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.model.Currency
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CurrencyExchangeTest {
    @Test
    fun `test USD to EUR conversion`() {
        val amount = BigDecimal("10.00")
        val conversion = CurrencyExchange.convert(amount, Currency.USD, Currency.EUR)
        assertEquals(BigDecimal("9.50"), conversion.result)
        assertEquals(BigDecimal("0.9500"), conversion.appliedRate)
    }

    @Test
    fun `test EUR to GBP conversion`() {
        val amount = BigDecimal("10.00")
        val conversion = CurrencyExchange.convert(amount, Currency.EUR, Currency.GBP)
        assertEquals(BigDecimal("8.31"), conversion.result)
        assertEquals(BigDecimal("0.8316"), conversion.appliedRate)
    }

    @Test
    fun `test GBP to USD conversion`() {
        val amount = BigDecimal("10.00")
        val conversion = CurrencyExchange.convert(amount, Currency.GBP, Currency.USD)
        assertEquals(BigDecimal("12.65"), conversion.result)
        assertEquals(BigDecimal("1.2658"), conversion.appliedRate)
    }

    @Test
    fun `test same currency conversion`() {
        val amount = BigDecimal("10.00")
        val conversion = CurrencyExchange.convert(amount, Currency.USD, Currency.USD)
        assertEquals(amount, conversion.result)
        assertEquals(BigDecimal("1.0000"), conversion.appliedRate)
    }
}