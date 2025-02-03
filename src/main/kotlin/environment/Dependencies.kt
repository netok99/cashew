package com.environment

import arrow.core.Either
import arrow.core.getOrElse
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import com.wallet.WalletService
import com.wallet.walletService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.runBlocking

class Dependencies(
    val walletService: WalletService
)

suspend fun ResourceScope.dependencies(env: Env): Dependencies {
    val database = setupDatabase(hikari(env.dataSource))
    return Dependencies(
        walletService = walletService(database),
    )
}

fun setupDatabase(dataSource: HikariDataSource): Database = runBlocking {
    val database = Database(dataSource)
//    Either
//        .catch {
//            database.createTables()
//        }.map {
//            LOGGER.trace("Tables created.")
//        }.getOrElse {
//            LOGGER.warn("Something went wrong while trying to create the tables! Retrying again later.")
//            Thread.sleep(2_000)
//        }
    return@runBlocking database
}

suspend fun ResourceScope.hikari(env: Env.DataSource): HikariDataSource = autoCloseable {
    HikariDataSource(
        HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            isAutoCommit = true
            leakDetectionThreshold = 30L * 1000
            transactionIsolation = IsolationLevel.TRANSACTION_READ_COMMITTED.name
            maximumPoolSize = System.getenv("RINHA_POOL_SIZE")?.toInt() ?: 8
            poolName = "CashewPool"
            jdbcUrl = env.url
            username = env.username
            password = env.password
            driverClassName = env.driver
        }
    )
}
