package com.transaction

import com.environment.Database
import java.sql.ResultSet

fun transactionService(database: Database): TransactionService = object : TransactionService {
    override suspend fun getTransactions(): List<TransactionModel> = database.withConnection {
        val stmt = it.prepareStatement("SELECT id, account_id, amount, mcc, merchant, created_at FROM transaction")
        val resultSet = stmt.executeQuery()
        return@withConnection mutableListOf<TransactionModel>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToTransactionModel(resultSet))
            }
        }
    }

    override suspend fun getTransaction(accountId: Int): List<TransactionModel> = database.withConnection {
        val stmt = it.prepareStatement(
            """SELECT id, account_id, amount, mcc, merchant, created_at FROM transaction WHERE "id" = $accountId"""
        )
        val resultSet = stmt.executeQuery()
        return@withConnection mutableListOf<TransactionModel>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToTransactionModel(resultSet))
            }
        }
    }

    override suspend fun createTransaction(transaction: Transaction) {
        database.withConnection {
            val stmt = it.prepareStatement(
                "INSERT INTO transaction (account_id, amount, merchant, mcc) VALUES " +
                        "(${transaction.accountId.value}, ${transaction.amount.value}, " +
                        "'${transaction.merchant.value}', ${transaction.mcc.value}) RETURNING id;"
            )
            stmt.executeQuery()
        }
    }

    fun convertToResultSetToTransactionModel(resultSet: ResultSet) = TransactionModel(
        id = resultSet.getInt(1),
        accountId = resultSet.getInt(2),
        amount = resultSet.getDouble(3),
        merchant = resultSet.getString(4),
        mcc = resultSet.getString(5)
    )
}
