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
                    DROP TABLE IF EXISTS account CASCADE;
                    DROP TABLE IF EXISTS accounts CASCADE;
                        
                    CREATE TABLE IF NOT EXISTS account(
                         id SERIAL PRIMARY KEY,
                         username VARCHAR(255) UNIQUE NOT NULL
                     );
                     
                    INSERT INTO account (username) VALUES ('Edson Arantes do Nascimento');
                                       
                    DROP TABLE IF EXISTS wallets CASCADE;
                    DROP TABLE IF EXISTS wallet CASCADE;
                                                               
                    CREATE TABLE IF NOT EXISTS wallet(
                        id SERIAL PRIMARY KEY,
                        account_id BIGINT UNIQUE NOT NULL REFERENCES account,
                        food DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                        meal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                        cash DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                        updated_at TIMESTAMP WITH TIME ZONE
                    );
                    
                    INSERT INTO wallet (account_id, food, meal, cash) VALUES (1, 500.0, 500.0, 500.0);

                    DROP TABLE IF EXISTS transactions CASCADE;
                    DROP TABLE IF EXISTS transaction CASCADE;
                    
                    CREATE TABLE IF NOT EXISTS transaction(
                        id SERIAL PRIMARY KEY,
                        account_id BIGINT NOT NULL REFERENCES account,
                        amount DECIMAL(10, 2) NOT NULL,
                        mcc VARCHAR(4) NOT NULL,
                        merchant VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE
                    );
                    """.trimIndent()
                )
        }
    }
}
