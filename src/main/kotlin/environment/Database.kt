package com.environment

import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

class Database(private val dataSource: HikariDataSource) {

    private val threadPool = Executors.newCachedThreadPool().asCoroutineDispatcher()

    private val permits = Semaphore(dataSource.maximumPoolSize * 4)

    suspend fun <T> withConnection(block: (Connection) -> (T)) = permits.withPermit {
        withContext(threadPool) {
            dataSource.connection.use {
                return@use block(it)
            }
        }
    }

    suspend fun createTables() {
        withConnection {
            it
                .createStatement()
                .execute(
                    """
                         CREATE TABLE IF NOT EXISTS wallet(
                             "id" SERIAL PRIMARY KEY,
                             "food" DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                             "meal" DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                             "cash" DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                             "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                         );

                         CREATE TABLE IF NOT EXISTS transaction(
                             "id" SERIAL PRIMARY KEY,
                             "wallet_id" BIGINT NOT NULL REFERENCES Wallets,
                             "account" VARCHAR(255) NOT NULL,
                             "amount" DECIMAL(10, 2) NOT NULL,
                             "mcc" VARCHAR(4) NOT NULL,
                             "merchant" VARCHAR(255) NOT NULL,
                             "created_at" TIMESTAMP TIME ZONE NOT NULL CURRENT_TIMESTAMP
                         );

                    """.trimIndent()
                )
        }
    }
}
