package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.exceptions.InsufficientBalanceException
import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.exceptions.TransactionFailedException
import com.teya.interviews.ledger.model.AccountStatus
import com.teya.interviews.ledger.model.LedgerEntryType
import com.teya.interviews.ledger.model.TransactionType
import com.teya.interviews.ledger.model.dto.DTOConverter
import com.teya.interviews.ledger.model.dto.TransactionDTO
import com.teya.interviews.ledger.model.dto.TransactionRequestDTO
import com.teya.interviews.ledger.repository.AccountRepository
import com.teya.interviews.ledger.repository.LedgerRepository
import com.teya.interviews.ledger.repository.TransactionLogRepository
import com.teya.interviews.ledger.repository.entity.Account
import com.teya.interviews.ledger.repository.entity.LedgerEntry
import com.teya.interviews.ledger.repository.entity.Transaction
import jakarta.persistence.PersistenceException
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant

@Service
class TransactionService @Autowired constructor(
    private val accountDAO: AccountRepository,
    private val ledgerDAO: LedgerRepository,
    private val transactionDAO: TransactionLogRepository,
) {
    fun getTransactionDetails(userId: String, txId: Long): TransactionDTO {
        val transaction = transactionDAO.findById(txId)
        if(!transaction.isPresent) {
            throw ResourceNotFoundException("Transaction not found")
        }
        if(transaction.get().type == TransactionType.TRANSFER) {
            if(transaction.get().sourceAccount?.owner?.id != userId && transaction.get().destinationAccount.owner?.id != userId) {
                throw AccessDeniedException()
            }
        } else {
            if(transaction.get().destinationAccount.owner?.id != userId) {
                throw AccessDeniedException()
            }
        }
        return DTOConverter.MAPPER.map(transaction.get(), TransactionDTO::class.java)
    }
    fun executeTransaction(userId: String, request: TransactionRequestDTO, attempt: Int = 1): TransactionDTO {
        require(request.amount > BigDecimal.ZERO) { "Amount must be greater than 0" }
        if(attempt > 3) {
            throw TransactionFailedException()
        }
        try {
            if(TransactionType.DEPOSIT == request.type) {
                return executeDeposit(userId, request)
            } else if (TransactionType.WITHDRAWAL == request.type) {
                return executeWithdrawal(userId, request)
            } else if (TransactionType.TRANSFER == request.type) {
                return executeTransfer(userId, request)
            }
            throw IllegalArgumentException("Invalid transaction type")
        } catch(e: PersistenceException) {
            // Optimistic locking exception for example, connection failed...
            return executeTransaction(userId, request, attempt + 1)
        }
    }

    @Transactional
    private fun executeTransfer(userId: String, request: TransactionRequestDTO): TransactionDTO {
        val timestamp = Instant.now()
        val sourceAccount = loadAccountIfActiveAndBelongsToUser(userId, request.accountId)
        val destinationAccount = accountDAO.findById(request.toAccountId!!)
        if (!destinationAccount.isPresent || destinationAccount.get().status != AccountStatus.ACTIVE) {
            throw IllegalArgumentException("Destination account not found or does not accept transfers")
        }
        if (sourceAccount.balance < request.amount) {
            throw InsufficientBalanceException("Insufficient balance")
        }
        val exchange = CurrencyExchange.convert(request.amount, sourceAccount.currency, destinationAccount.get().currency)
        val transaction = Transaction(
            sourceAccount = sourceAccount,
            destinationAccount = destinationAccount.get(),
            amount = request.amount,
            concept = request.concept,
            timestamp = timestamp,
            type = request.type,
            originCurrency = sourceAccount.currency,
            destinationCurrency = destinationAccount.get().currency,
            convertedAmount = exchange.result,
            appliedConversionRate = exchange.appliedRate
        )
        val result = transactionDAO.save(transaction)
        // update balances
        sourceAccount.balance = sourceAccount.balance.subtract(request.amount)
        destinationAccount.get().balance = destinationAccount.get().balance.add(exchange.result)
        // create source ledger entries
        val sourceEntry = LedgerEntry(
            account = sourceAccount,
            amount = request.amount.negate(),
            concept = request.concept?: "Transfer to ${destinationAccount.get().id}",
            timestamp = timestamp,
            type = LedgerEntryType.DEBIT,
            transaction = result
        )
        val destinationEntry = LedgerEntry(
            account = destinationAccount.get(),
            amount = exchange.result,
            concept = request.concept?: "Transfer received from ${sourceAccount.id}",
            timestamp = timestamp,
            type = LedgerEntryType.CREDIT,
            transaction = result
        )
        accountDAO.save(sourceAccount)
        accountDAO.save(destinationAccount.get())
        ledgerDAO.save(sourceEntry)
        ledgerDAO.save(destinationEntry)
        return DTOConverter.MAPPER.map(result, TransactionDTO::class.java)
    }

    private fun executeWithdrawal(userId: String, request: TransactionRequestDTO): TransactionDTO {
        val account = loadAccountIfActiveAndBelongsToUser(userId, request.accountId)
        if (account.balance < request.amount) {
            throw InsufficientBalanceException("Insufficient balance")
        }
        return executeAccountOperation(account, request, request.amount.negate(), LedgerEntryType.DEBIT)
    }

    private fun executeDeposit(userId: String, request: TransactionRequestDTO): TransactionDTO {
        val account = loadAccountIfActiveAndBelongsToUser(userId, request.accountId)
        return executeAccountOperation(account, request, request.amount, LedgerEntryType.CREDIT)
    }

    private fun loadAccountIfActiveAndBelongsToUser(userId: String, accountId: Long): Account {
        val account = accountDAO.findById(accountId)
        if (!account.isPresent || userId != account.get().owner.id) {
            throw AccessDeniedException()
        }
        require(account.get().status == AccountStatus.ACTIVE) { "Account is ${account.get().status}" }
        return account.get()
    }
    @Transactional
    private fun executeAccountOperation(account: Account, request: TransactionRequestDTO, ledgerAmount: BigDecimal, entryType: LedgerEntryType): TransactionDTO{

        val timestamp = Instant.now()
        // create transaction log entry
        val transaction = Transaction(
            destinationAccount = account,
            amount = request.amount,
            concept = request.concept,
            timestamp = timestamp,
            type = request.type,
            destinationCurrency = account.currency
        )
        val result = transactionDAO.save(transaction)

        //update account balance
        account.balance = account.balance.add(ledgerAmount)
        //create ledger entry
        val entry = LedgerEntry(
            account = account,
            amount = ledgerAmount,
            concept = request.concept?: request.type.name.lowercase(),
            timestamp = timestamp,
            type = entryType,
            transaction = result
        )

        accountDAO.save(account)
        ledgerDAO.save(entry)
        return DTOConverter.MAPPER.map(result, TransactionDTO::class.java)
    }

}
