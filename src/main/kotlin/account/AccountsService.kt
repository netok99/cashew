package com.account

import com.environment.Database
import java.sql.ResultSet

private const val USERNAME_PARAMETER = "{username}"
private const val GET_ACCOUNTS_QUERY = "SELECT id, username FROM account ORDER BY id;"
private const val CREATE_ACCOUNT_QUERY =
    "INSERT INTO account (username) VALUES ('$USERNAME_PARAMETER') RETURNING id, username;"

fun accountService(database: Database): AccountService = object : AccountService {
    override suspend fun getAccounts(): List<Account> = database.withConnection {
        val preparedStatement = it.prepareStatement(GET_ACCOUNTS_QUERY)
        val resultSet = preparedStatement.executeQuery()
        return@withConnection mutableListOf<Account>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToAccountModel(resultSet))
            }
        }
    }

    override suspend fun createAccount(username: String): Account = database.withConnection {
        val preparedStatement = it
            .prepareStatement(CREATE_ACCOUNT_QUERY.replace(USERNAME_PARAMETER, username))
        val resultSet = preparedStatement.executeQuery().apply { this.next() }
        return@withConnection convertToResultSetToAccountModel(resultSet)
    }

    private fun convertToResultSetToAccountModel(resultSet: ResultSet) = Account(
        id = resultSet.getInt(1),
        username = resultSet.getString(2)
    )
}
