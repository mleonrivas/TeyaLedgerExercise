package com.teya.interviews.ledger.repository

import com.teya.interviews.ledger.repository.entity.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface AccountRepository: JpaRepository<Account, Long> {
    @Query("SELECT a FROM Account a WHERE a.owner.id = :uid AND a.status != AccountStatus.CLOSED")
    fun findOpenAccountsByOwner(@Param("uid") uid: String): List<Account>

}