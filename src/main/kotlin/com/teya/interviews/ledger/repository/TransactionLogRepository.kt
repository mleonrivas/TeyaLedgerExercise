package com.teya.interviews.ledger.repository

import com.teya.interviews.ledger.repository.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionLogRepository : JpaRepository<Transaction, Long> {

}