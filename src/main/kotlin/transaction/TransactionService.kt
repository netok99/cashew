package com.transaction

import arrow.core.Either
import arrow.core.raise.either
import com.environment.Database
import java.sql.ResultSet

private const val ACCOUNT_ID_PARAMETER = "{accountId}"
private const val AMOUNT_ID_PARAMETER = "{amount}"
private const val MERCHANT_ID_PARAMETER = "{merchant}"
private const val MCC_ID_PARAMETER = "{mcc}"
private const val NAME_PARAMETER = "{name}"
private const val GET_TRANSACTIONS_QUERY =
    "SELECT id, account_id, amount, mcc, merchant, created_at FROM transaction ORDER BY id;"
private const val CREATE_TRANSACTION_QUERY =
    "INSERT INTO transaction (account_id, amount, merchant, mcc) VALUES ($ACCOUNT_ID_PARAMETER, $AMOUNT_ID_PARAMETER," +
        " '$MERCHANT_ID_PARAMETER', '$MCC_ID_PARAMETER') RETURNING id;"
private const val GET_MCC_QUERY = """SELECT mcc FROM MERCHANT where "name" = '$NAME_PARAMETER';"""

fun transactionService(database: Database): TransactionService = object : TransactionService {
    override suspend fun getTransactions(): Either<String, List<Transaction>> = database.withConnection {
        val prepareStatement = it.prepareStatement(GET_TRANSACTIONS_QUERY)
        val resultSet = prepareStatement.executeQuery()
        val transactions = mutableListOf<TransactionModel>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToTransactionModel(resultSet))
            }
        }
        val validatedTransactions = either { transactions.map(::validateAndTransformToTransaction).bindAll() }
        return@withConnection validatedTransactions
    }

    override suspend fun createTransaction(transaction: Transaction) {
        database.withConnection {
            val prepareStatement = it.prepareStatement(
                CREATE_TRANSACTION_QUERY
                    .replace(ACCOUNT_ID_PARAMETER, transaction.accountId.value.toString())
                    .replace(AMOUNT_ID_PARAMETER, transaction.amount.value.toString())
                    .replace(MERCHANT_ID_PARAMETER, transaction.merchant.value)
                    .replace(MCC_ID_PARAMETER, transaction.mcc.value)
            )
            prepareStatement.executeQuery()
        }
    }

    override suspend fun getMcc(transaction: Transaction): String = database.withConnection {
        val prepareStatement = it.prepareStatement(GET_MCC_QUERY.replace(NAME_PARAMETER, transaction.merchant.value))
        val resultSet = prepareStatement.executeQuery().apply { this.next() }
        return@withConnection resultSet.getString(1)
    }

    private fun convertToResultSetToTransactionModel(resultSet: ResultSet) = TransactionModel(
        id = resultSet.getInt(1),
        accountId = resultSet.getInt(2),
        amount = resultSet.getDouble(3),
        mcc = resultSet.getString(4),
        merchant = resultSet.getString(5)
    )
}
