package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.*
import com.teya.interviews.ledger.model.*
import com.teya.interviews.ledger.model.Currency
import com.teya.interviews.ledger.model.dto.*
import com.teya.interviews.ledger.repository.*
import com.teya.interviews.ledger.repository.entity.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class TransactionServiceTest {
    private val accountDAO = mock(AccountRepository::class.java)
    private val ledgerDAO = mock(LedgerRepository::class.java)
    private val transactionDAO = mock(TransactionLogRepository::class.java)
    private val transactionService = TransactionService(accountDAO, ledgerDAO, transactionDAO)

    private val USER_1 = "user1"
    private val USER_2 = "user2"
    private val ACCOUNT_1 = Account(id = 1L, owner = User(id = USER_1), alias = "Account 1", balance = BigDecimal("1000.00"), status = AccountStatus.ACTIVE, currency = Currency.GBP)
    private val ACCOUNT_2 = Account(id = 2L, owner = User(id = USER_2), alias = "Account 2", balance = BigDecimal("500.00"), status = AccountStatus.ACTIVE, currency = Currency.EUR)
    @Test
    fun `test getTransactionDetails with valid data`() {
        val txId = 1L
        val transaction = Transaction(
            id = txId,
            sourceAccount = ACCOUNT_1,
            destinationAccount = ACCOUNT_2,
            type = TransactionType.TRANSFER,
            amount = BigDecimal("100.00"),
            concept = "transfer",
            timestamp = Instant.now(),
            originCurrency = ACCOUNT_1.currency,
            destinationCurrency = ACCOUNT_2.currency,
            appliedConversionRate = BigDecimal("1.2000"),
            convertedAmount = BigDecimal("120.00")
        )
        `when`(transactionDAO.findById(txId)).thenReturn(Optional.of(transaction))

        val result = transactionService.getTransactionDetails(USER_2, txId)
        assertNotNull(result)
        checkToDTOTransformation(transaction, result)
    }

    @Test
    fun `test getTransactionDetails with invalid transaction`() {
        val userId = "user1"
        val txId = 1L
        `when`(transactionDAO.findById(txId)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            transactionService.getTransactionDetails(userId, txId)
        }
    }
    @Test
    fun `test IllegalArgumentException with negative amount`() {
        val request = TransactionRequestDTO(
            accountId = 1L,
            amount = BigDecimal(-1),
            type = TransactionType.DEPOSIT
        )

        assertThrows<IllegalArgumentException> {
            transactionService.executeTransaction(USER_1, request)
        }
    }

    @Test
    fun `test AccessDeniedException when transacting from an unowned account`() {
        val request = TransactionRequestDTO(
            accountId = 1L,
            amount = BigDecimal(1),
            type = TransactionType.WITHDRAWAL
        )
        assertThrows<AccessDeniedException> {
            transactionService.executeTransaction(USER_2, request)
        }
    }

    @Test
    fun `test insufficient balance when withdrawing with a higher amount`() {
        val request = TransactionRequestDTO(
            accountId = 1L,
            amount = BigDecimal(10000),
            type = TransactionType.WITHDRAWAL
        )
        `when`(accountDAO.findById(request.accountId)).thenReturn(Optional.of(ACCOUNT_1))
        assertThrows<InsufficientBalanceException> {
            transactionService.executeTransaction(USER_1, request)
        }
    }
    @Test
    fun `test executeTransaction with deposit`() {
        val userId = "user1"
        val request = TransactionRequestDTO(
            accountId = 1L,
            amount = BigDecimal("100.00"),
            type = TransactionType.DEPOSIT,
            concept = "monthly savings"
        )
        `when`(accountDAO.findById(request.accountId)).thenReturn(Optional.of(ACCOUNT_1))
        val txCaptor = argumentCaptor<Transaction>()
        `when`(transactionDAO.save(txCaptor.capture())).thenAnswer { it.arguments[0] }

        val result = transactionService.executeTransaction(userId, request)
        assertNotNull(result)
        verify(accountDAO).save(argThat { it.id == ACCOUNT_1.id && it.balance == BigDecimal("1100.00") })
        verify(ledgerDAO).save(argThat { it.account.id == ACCOUNT_1.id && it.type == LedgerEntryType.CREDIT && it.amount == BigDecimal("100.00") })
    }

    @Test
    fun `test executeTransaction with withdrawal`() {
        val userId = "user1"
        val request = TransactionRequestDTO(
            accountId = 1L,
            amount = BigDecimal("100.00"),
            type = TransactionType.WITHDRAWAL
        )
        `when`(accountDAO.findById(request.accountId)).thenReturn(Optional.of(ACCOUNT_1))
        val txCaptor = argumentCaptor<Transaction>()
        `when`(transactionDAO.save(txCaptor.capture())).thenAnswer { it.arguments[0] }

        val result = transactionService.executeTransaction(userId, request)
        assertNotNull(result)
        verify(accountDAO).save(argThat { it.id == ACCOUNT_1.id && it.balance == BigDecimal("900.00") })
        verify(ledgerDAO).save(argThat { it.account.id == ACCOUNT_1.id && it.type == LedgerEntryType.DEBIT && it.amount == BigDecimal("-100.00") })
    }

    @Test
    fun `test executeTransaction with transfer`() {
        val userId = "user1"
        val request = TransactionRequestDTO(
            accountId = 1L,
            toAccountId = 2L,
            amount = BigDecimal("100.00"),
            type = TransactionType.TRANSFER
        )
        val sourceAccount = Account(id = 1L, owner = User(id = userId), balance = BigDecimal("1000.00"), status = AccountStatus.ACTIVE, currency = Currency.USD)
        val destinationAccount = Account(id = 2L, owner = User(id = "user2"), balance = BigDecimal("500.00"), status = AccountStatus.ACTIVE, currency = Currency.EUR)
        `when`(accountDAO.findById(request.accountId)).thenReturn(Optional.of(sourceAccount))
        `when`(accountDAO.findById(request.toAccountId!!)).thenReturn(Optional.of(destinationAccount))
        val txCaptor = argumentCaptor<Transaction>()
        `when`(transactionDAO.save(txCaptor.capture())).thenAnswer { it.arguments[0] }
        val result = transactionService.executeTransaction(userId, request)
        assertNotNull(result)
    }

    private fun checkToDTOTransformation(transaction: Transaction, result: TransactionDTO) {
        assertEquals(transaction.id, result.id)
        if(transaction.sourceAccount != null) {
            assertEquals(transaction.sourceAccount!!.id, result.sourceAccountId)
        }
        assertEquals(transaction.destinationAccount.id, result.destinationAccountId)
        assertEquals(transaction.amount, result.amount)
        assertEquals(transaction.concept, result.concept)
        assertEquals(transaction.timestamp, result.timestamp)
        assertEquals(transaction.type, result.type)
        assertEquals(transaction.destinationCurrency, result.destinationCurrency)
        assertEquals(transaction.originCurrency, result.originCurrency)
        assertEquals(transaction.appliedConversionRate, result.appliedConversionRate)
        assertEquals(transaction.convertedAmount, result.convertedAmount)
    }
}