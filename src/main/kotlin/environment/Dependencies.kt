package com.environment

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import com.account.AccountUseCase
import com.account.accountService
import com.transaction.TransactionUseCase
import com.transaction.transactionService
import com.wallet.WalletUseCase
import com.wallet.walletService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.runBlocking

class Dependencies(
    val accountUseCase: AccountUseCase,
    val transactionUseCase: TransactionUseCase,
    val walletUseCase: WalletUseCase
)

suspend fun ResourceScope.dependencies(env: Env): Dependencies {
    val database = setupDatabase(hikari(env.dataSource))
    val walletUseCase = WalletUseCase(walletService(database))
    return Dependencies(
        accountUseCase = AccountUseCase(
            accountService = accountService(database),
            walletUseCase = walletUseCase
        ),
        transactionUseCase = TransactionUseCase(
            transactionService = transactionService(database),
            walletUseCase = walletUseCase
        ),
        walletUseCase = walletUseCase
    )
}

fun setupDatabase(dataSource: HikariDataSource): Database = runBlocking {
    Database(dataSource).apply { createTables() }
}

suspend fun ResourceScope.hikari(env: Env.DataSource): HikariDataSource = autoCloseable {
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
}
