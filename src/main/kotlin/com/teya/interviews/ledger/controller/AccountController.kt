package com.teya.interviews.ledger.controller

import com.teya.interviews.ledger.context.AuthHelper
import com.teya.interviews.ledger.model.dto.AccountDTO
import com.teya.interviews.ledger.model.dto.CreateAccountRequestDTO
import com.teya.interviews.ledger.model.dto.LedgerEntryDTO
import com.teya.interviews.ledger.service.AccountService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/accounts")
class AccountController @Autowired constructor(
    private val accountService: AccountService
){
    @Operation(summary = "List User Accounts", description = "Retrieves all accounts for the authenticated user")
    @GetMapping
    fun listAccountsForUser(): ResponseEntity<List<AccountDTO>> {
        val userId = AuthHelper.getAuthenticatedUserId()
        val result = accountService.listAccountsForUser(userId)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Create an account", description = "Creates a new account, only available to ADMIN users")
    @PostMapping
    fun createAccount(@RequestBody request: CreateAccountRequestDTO): ResponseEntity<AccountDTO> {
        AuthHelper.requireAdmin()
        val result = accountService.createAccount(request)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "Get Account Details")
    @GetMapping("/{id}")
    fun getAccountDetails(@PathVariable id: Long): ResponseEntity<AccountDTO> {
        val userId = AuthHelper.getAuthenticatedUserId()
        val account = accountService.getAccountDetails(userId, id)
        return ResponseEntity.ok(account)
    }

    @Operation(summary = "Get Ledger Entries", description = "Retrieves ledger entries for the account")
    @GetMapping("/{id}/ledger")
    fun getAccountLedger(@PathVariable id: Long,
                         @RequestParam(required=false) start: Instant = Instant.now().minusSeconds(60*60*24*90),
                         @RequestParam(required=false) end: Instant = Instant.now()
    ): ResponseEntity<List<LedgerEntryDTO>> {
        val userId = AuthHelper.getAuthenticatedUserId()
        val ledger = accountService.getLedgerForAccount(userId, id, start, end)
        return ResponseEntity.ok(ledger)
    }

}
