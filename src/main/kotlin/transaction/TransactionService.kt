package com.transaction

import arrow.core.Either
import arrow.core.raise.either
import com.environment.Database
import java.sql.ResultSet

fun transactionService(database: Database): TransactionService = object : TransactionService {
    override suspend fun getTransactions(): Either<String, List<Transaction>> = database.withConnection {
        val stmt = it.prepareStatement("SELECT id, account_id, amount, mcc, merchant, created_at FROM transaction")
        val resultSet = stmt.executeQuery()
        val transactions = mutableListOf<TransactionModel>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToTransactionModel(resultSet))
            }
        }
        val validatedTransactions = either {
            transactions.map(::validateAndTransformToTransaction).bindAll()
        }
        return@withConnection validatedTransactions
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
