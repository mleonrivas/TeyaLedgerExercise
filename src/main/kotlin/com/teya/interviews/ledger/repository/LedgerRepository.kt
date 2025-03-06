package com.teya.interviews.ledger.repository

import com.teya.interviews.ledger.repository.entity.LedgerEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface LedgerRepository : JpaRepository<LedgerEntry, Long> {
    @Query("SELECT e FROM LedgerEntry e WHERE e.account.id = :aid AND e.timestamp >= :start AND e.timestamp <= :end ORDER BY e.timestamp DESC" )
    fun findByAccountAndTimeBetween(@Param("aid") aid: Long, @Param("start") start: Instant, @Param("end") end: Instant): List<LedgerEntry>
}