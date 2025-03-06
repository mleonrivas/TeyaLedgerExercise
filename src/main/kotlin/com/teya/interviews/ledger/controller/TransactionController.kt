package com.teya.interviews.ledger.controller

import com.teya.interviews.ledger.context.AuthHelper
import com.teya.interviews.ledger.model.dto.TransactionDTO
import com.teya.interviews.ledger.model.dto.TransactionRequestDTO
import com.teya.interviews.ledger.service.TransactionService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/transactions")
class TransactionController @Autowired constructor(
    private val transactionService: TransactionService
){

    @Operation(summary = "creates a new transaction")
    @PostMapping
    fun createTransaction(@RequestBody request: TransactionRequestDTO): ResponseEntity<TransactionDTO> {
        val userId = AuthHelper.getAuthenticatedUserId()
        val tx = transactionService.executeTransaction(userId, request)
        return ResponseEntity.ok(tx)
    }

    @Operation(summary = "Get Transaction Details")
    @GetMapping("/{id}")
    fun getTransactionDetails(@RequestParam id: Long): ResponseEntity<TransactionDTO> {
        val userId = AuthHelper.getAuthenticatedUserId()
        val tx = transactionService.getTransactionDetails(userId, id)
        return ResponseEntity.ok(tx)
    }
}