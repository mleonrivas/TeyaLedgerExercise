package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.model.AccountStatus
import com.teya.interviews.ledger.model.Currency
import com.teya.interviews.ledger.model.LedgerEntryType
import com.teya.interviews.ledger.model.dto.CreateAccountRequestDTO
import com.teya.interviews.ledger.repository.AccountRepository
import com.teya.interviews.ledger.repository.LedgerRepository
import com.teya.interviews.ledger.repository.UserRepository
import com.teya.interviews.ledger.repository.entity.Account
import com.teya.interviews.ledger.repository.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.*
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class AccountServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var ledgerRepository: LedgerRepository
    private lateinit var userRepository: UserRepository
    private lateinit var accountService: AccountService

    @BeforeEach
    fun setUp() {
        accountRepository = mock(AccountRepository::class.java)
        ledgerRepository = mock(LedgerRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        accountService = AccountService(accountRepository, ledgerRepository, userRepository)
    }

    @Test
    fun `listAccountsForUser should return list of accounts`() {
        val userId = "user1"
        val account = Account(id = 1L, owner = User(id = userId), balance = BigDecimal(1000), currency = Currency.GBP, status = AccountStatus.ACTIVE, alias = "Checking")
        whenever(accountRepository.findOpenAccountsByOwner(userId)).thenReturn(listOf(account))

        val accounts = accountService.listAccountsForUser(userId)

        assertEquals(1, accounts.size)
        assertEquals(1L, accounts[0].id)
    }

    @Test
    fun `getAccountDetails should return account details`() {
        val userId = "user1"
        val accountId = 1L
        val account = Account(id = accountId, owner = User(id = userId), balance = BigDecimal(1000), currency = Currency.GBP, status = AccountStatus.ACTIVE, alias = "Checking")
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        val accountDetails = accountService.getAccountDetails(userId, accountId)

        assertEquals(accountId, accountDetails.id)
    }

    @Test
    fun `getAccountDetails should throw AccessDeniedException if user is not owner`() {
        val userId = "user1"
        val accountId = 1L
        val account = Account(id = accountId, owner = User(id = "user2"), balance = BigDecimal(1000), currency = Currency.GBP, status = AccountStatus.ACTIVE, alias = "Checking")
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        assertThrows<AccessDeniedException> {
            accountService.getAccountDetails(userId, accountId)
        }
    }

    @Test
    fun `createAccount should create and return account`() {
        val userId = "user1"
        val request = CreateAccountRequestDTO(ownerId = userId, initialBalance = BigDecimal("1000.00"), currency = Currency.GBP, alias = "Checking")
        val user = User(id = userId)
        val account = Account(id = 1L, owner = user, balance = BigDecimal("1000.00"), currency = Currency.GBP, status = AccountStatus.ACTIVE, alias = "Checking")
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(accountRepository.save(any(Account::class.java))).thenReturn(account)

        val createdAccount = accountService.createAccount(request)

        assertEquals(1L, createdAccount.id)
        verify(ledgerRepository).save(argThat { it.account.id == 1L && it.type == LedgerEntryType.CREDIT && it.amount == BigDecimal("1000.00") })
    }

    @Test
    fun `createAccount should throw ResourceNotFoundException if owner does not exist`() {
        val userId = "user1"
        val request = CreateAccountRequestDTO(ownerId = userId, initialBalance = BigDecimal(1000), currency = Currency.GBP, alias = "Checking")
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            accountService.createAccount(request)
        }
    }

    @Test
    fun `createAccount should throw IllegalArgumentException if initial balance is negative`() {
        val userId = "user1"
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(User(id = userId)))
        val request = CreateAccountRequestDTO(ownerId = userId, initialBalance = BigDecimal(-1000), currency = Currency.GBP, alias = "Checking")

        assertThrows<IllegalArgumentException> {
            accountService.createAccount(request)
        }
    }
}