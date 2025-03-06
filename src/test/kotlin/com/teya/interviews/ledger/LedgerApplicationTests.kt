package com.teya.interviews.ledger

import com.teya.interviews.ledger.config.TestJpaConfig
import com.teya.interviews.ledger.controller.AccountController
import com.teya.interviews.ledger.controller.TransactionController
import com.teya.interviews.ledger.controller.UserController
import com.teya.interviews.ledger.exceptions.InsufficientBalanceException
import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.model.Currency
import com.teya.interviews.ledger.model.LedgerEntryType
import com.teya.interviews.ledger.model.TransactionType
import com.teya.interviews.ledger.model.UserRole
import com.teya.interviews.ledger.model.dto.CreateAccountRequestDTO
import com.teya.interviews.ledger.model.dto.CreateUserRequestDTO
import com.teya.interviews.ledger.model.dto.LedgerEntryDTO
import com.teya.interviews.ledger.model.dto.TransactionRequestDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import kotlin.test.assertTrue


@SpringBootTest
@ContextConfiguration(classes = [TestJpaConfig::class])
class LedgerApplicationTests {
	@Autowired
	private lateinit var accounts: AccountController
	@Autowired
	private lateinit var users: UserController
	@Autowired
	private lateinit var transactions: TransactionController

	private fun setupAuth(userId: String, role: UserRole) {
		SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken.authenticated(userId, null,
			listOf(SimpleGrantedAuthority(role.name)))
	}
	private fun <T> checkOkAndExtractEntity(response: ResponseEntity<T>): T {
		assert(response.statusCode.is2xxSuccessful)
		return response.body!!
	}

	private fun checkLedgerEntry(entry: LedgerEntryDTO, amount: BigDecimal, concept: String, type: LedgerEntryType) {
		assertEquals(amount, entry.amount)
		assertEquals(concept, entry.concept)
		assertEquals(type, entry.type)
	}

	@Test
	fun `test a complete script of the api`() {
		//1. as Admin: create users, create accounts for them
		setupAuth("admin", UserRole.ADMIN)
		// 1.1 Create a couple of users: Alice and Bob:
		val alice = checkOkAndExtractEntity(users.createUser(CreateUserRequestDTO(name ="Alice", email="alice@teya.com", phone = null, role = UserRole.USER)))
		val bob = checkOkAndExtractEntity(users.createUser(CreateUserRequestDTO(name ="Bob", email="bob@teya.com", phone = null, role = UserRole.USER)))

		// 1.2 Verify the users were created correctly, and copy their IDs:
		val allUsers = checkOkAndExtractEntity(users.listUsers())
		assertEquals(2, allUsers.size)
		assertTrue(allUsers.any { it.name == "Alice" })
		assertTrue(allUsers.any { it.name == "Bob" })

		// 1.3 Create the accounts for Alice and Bob:
		val aliceAccount = checkOkAndExtractEntity(accounts.createAccount(CreateAccountRequestDTO(currency = Currency.GBP, BigDecimal("1000.00"), alice.id!!, "Alice's Account")))
		val bobAccount = checkOkAndExtractEntity(accounts.createAccount(CreateAccountRequestDTO(currency = Currency.EUR, BigDecimal("5000.00"), bob.id!!, "Bob's Account")))

		// 2. Acting as Alice:
		setupAuth(alice.id!!, UserRole.USER)
		// 2.1 Try creating an account (Should fail with 403 as Alice is not admin):
		assertThrows<AccessDeniedException> {
			accounts.createAccount(CreateAccountRequestDTO(currency = Currency.EUR, BigDecimal("5000.00"), alice.id!!, "Alice's 2nd Account"))
		}

		// 2.2 List alice accounts (only alice's account should be visible):
		val aliceAccounts = checkOkAndExtractEntity(accounts.listAccountsForUser())
		assertEquals(1, aliceAccounts.size)
		assertEquals(aliceAccount.id, aliceAccounts[0].id)
		assertEquals(aliceAccount.balance, aliceAccounts[0].balance)

		// 2.3 Do a deposit to Alice's account:
		checkOkAndExtractEntity(transactions.createTransaction(
			TransactionRequestDTO(type= TransactionType.DEPOSIT, amount = BigDecimal("100.00"), accountId = aliceAccount.id!!, concept = "Additional Deposit")
		))

		// 2.4 Do a withdrawal from Alice's account of 2000 GBP, should fail with insufficient balance:
		assertThrows<InsufficientBalanceException> {
			transactions.createTransaction(TransactionRequestDTO(type= TransactionType.WITHDRAWAL, amount = BigDecimal("2000.00"), accountId = aliceAccount.id!!, concept = "Invalid Withdrawal"))
		}

		// 2.5 Try another withdrawal with a valid amount (500 GBP):
		checkOkAndExtractEntity(transactions.createTransaction(
			TransactionRequestDTO(type= TransactionType.WITHDRAWAL, amount = BigDecimal("500.00"), accountId = aliceAccount.id!!, concept = "Valid Withdrawal")
		))

		// 2.6 Try withdrawing money from Bob's account (should fail with a 400):
		assertThrows<AccessDeniedException> {
			transactions.createTransaction(
				TransactionRequestDTO(type= TransactionType.WITHDRAWAL, amount = BigDecimal("100.00"), accountId = bobAccount.id!!, concept = "Invalid Withdrawal")
			)
		}

		// 2.7 Do a proper transfer from Alice to Bob:
		checkOkAndExtractEntity(
			transactions.createTransaction(
				TransactionRequestDTO(type= TransactionType.TRANSFER, amount = BigDecimal("300.00"), accountId = aliceAccount.id!!, toAccountId = bobAccount.id!!, concept = "Paying Bob")
			)
		)

		// 2.8 Check the Alice's account details (balance = 300 GBP):
		val updatedAliceAccount = checkOkAndExtractEntity(accounts.getAccountDetails(aliceAccount.id!!))
		assertEquals(BigDecimal("300.00"), updatedAliceAccount.balance)

		// 2.9 Check Alice's account ledger:
		val aliceLedger = checkOkAndExtractEntity(accounts.getAccountLedger(aliceAccount.id!!))
		assertEquals(4, aliceLedger.size)
		checkLedgerEntry(aliceLedger[0], BigDecimal("-300.00"), "Paying Bob", LedgerEntryType.DEBIT)
		checkLedgerEntry(aliceLedger[1], BigDecimal("-500.00"), "Valid Withdrawal", LedgerEntryType.DEBIT)
		checkLedgerEntry(aliceLedger[2], BigDecimal("100.00"), "Additional Deposit", LedgerEntryType.CREDIT)
		checkLedgerEntry(aliceLedger[3], BigDecimal("1000.00"), "Initial deposit", LedgerEntryType.CREDIT)

		// 3. Acting as Bob:
		setupAuth(bob.id!!, UserRole.USER)
		// 3.1 Check Bob's account details (balance = 5360.75 EUR --- 300 GBP ~ 360.75 EUR):
		val updatedBobAccount = checkOkAndExtractEntity(accounts.getAccountDetails(bobAccount.id!!))
		assertEquals(BigDecimal("5360.75"), updatedBobAccount.balance)

		// 3.2 Check Bob's account ledger:
		val bobLedger = checkOkAndExtractEntity(accounts.getAccountLedger(bobAccount.id!!))
		assertEquals(2, bobLedger.size)
		checkLedgerEntry(bobLedger[0], BigDecimal("360.75"), "Paying Bob", LedgerEntryType.CREDIT)
		checkLedgerEntry(bobLedger[1], BigDecimal("5000.00"), "Initial deposit", LedgerEntryType.CREDIT)

	}

}
