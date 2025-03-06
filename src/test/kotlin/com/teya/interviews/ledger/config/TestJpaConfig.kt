package com.teya.interviews.ledger.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@TestConfiguration
class TestJpaConfig {

    @Bean
    fun dataSource(): DataSource {
        return DriverManagerDataSource().apply {
            setDriverClassName("org.h2.Driver")
            url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
            username = "sa"
            password = ""
        }
    }
}