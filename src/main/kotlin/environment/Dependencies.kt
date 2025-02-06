package com.environment

import com.account.AccountService
import com.account.accountService
import com.transaction.TransactionService
import com.transaction.transactionService
import com.wallet.WalletService
import com.wallet.walletService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.runBlocking

class Dependencies(
    val accountService: AccountService,
    val transactionService: TransactionService,
    val walletService: WalletService
)

fun dependencies(env: Env): Dependencies {
    val database = setupDatabase(hikari(env.dataSource))
    return Dependencies(
        accountService = accountService(database),
        transactionService = transactionService(database),
        walletService = walletService(database)
    )
}

private fun setupDatabase(dataSource: HikariDataSource): Database =
    runBlocking {
        Database(dataSource).apply { createTables() }
    }

private fun hikari(env: Env.DataSource): HikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            driverClassName = env.driver
            isAutoCommit = env.isAutoCommit
            leakDetectionThreshold = env.leakDetectionThreshold
            transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name
            maximumPoolSize = env.maximumPoolSize
            poolName = env.poolName
            jdbcUrl = env.url
            username = env.username
            password = env.password
            driverClassName = env.driver
        }
    )
