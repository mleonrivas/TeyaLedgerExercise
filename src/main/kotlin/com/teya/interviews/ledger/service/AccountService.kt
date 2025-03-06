package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.model.AccountStatus
import com.teya.interviews.ledger.model.LedgerEntryType
import com.teya.interviews.ledger.model.dto.AccountDTO
import com.teya.interviews.ledger.model.dto.CreateAccountRequestDTO
import com.teya.interviews.ledger.model.dto.DTOConverter
import com.teya.interviews.ledger.model.dto.LedgerEntryDTO
import com.teya.interviews.ledger.repository.AccountRepository
import com.teya.interviews.ledger.repository.LedgerRepository
import com.teya.interviews.ledger.repository.UserRepository
import com.teya.interviews.ledger.repository.entity.Account
import com.teya.interviews.ledger.repository.entity.LedgerEntry
import jakarta.transaction.Transactional

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.Instant


@Service
class AccountService @Autowired constructor(
    private val accountDAO: AccountRepository,
    private val ledgerDAO: LedgerRepository,
    private val userDAO: UserRepository
) {
    fun listAccountsForUser(userId: String): List<AccountDTO> {
        return accountDAO.findOpenAccountsByOwner(userId).map { a -> DTOConverter.MAPPER.map(a, AccountDTO::class.java) }
    }

    fun getAccountDetails(userId: String, accountId: Long): AccountDTO {
        val account = accountDAO.findById(accountId)
        if (!account.isPresent || userId != account.get().owner.id) {
            throw AccessDeniedException()
        }

        return DTOConverter.MAPPER.map(account.get(), AccountDTO::class.java)
    }

    fun getLedgerForAccount(userId: String, accountId: Long, startTime: Instant, endTime: Instant): List<LedgerEntryDTO> {
        val account = accountDAO.findById(accountId)
        if (!account.isPresent || userId != account.get().owner.id) {
            throw AccessDeniedException()
        }

        val entries = ledgerDAO.findByAccountAndTimeBetween(accountId, startTime, endTime)

        return entries.map { e -> DTOConverter.MAPPER.map(e, LedgerEntryDTO::class.java) }
    }

    @Transactional
    fun createAccount(request: CreateAccountRequestDTO): AccountDTO {
        val owner = userDAO.findById(request.ownerId!!)
        if (owner.isEmpty) {
            throw ResourceNotFoundException("ownerId ${request.ownerId} not found")
        }
        if (request.initialBalance!! < BigDecimal.ZERO) {
            throw IllegalArgumentException("initialBalance cannot be negative")
        }
        val entity = Account(
            currency = request.currency!!,
            balance = request.initialBalance!!,
            owner = owner.get(),
            alias = request.alias,
            status = AccountStatus.ACTIVE
        )
        val saved = accountDAO.save(entity)
        val entry = LedgerEntry(
            account = saved,
            amount = request.initialBalance!!,
            concept = "Initial deposit",
            timestamp = Instant.now(),
            type = LedgerEntryType.CREDIT
        )
        ledgerDAO.save(entry)
        return DTOConverter.MAPPER.map(saved, AccountDTO::class.java)
    }

}
