package com.account

import com.environment.Database
import java.sql.ResultSet

fun accountService(database: Database): AccountService = object : AccountService {
    override suspend fun getAccounts(): List<AccountModel> = database.withConnection {
        val stmt = it.prepareStatement("SELECT id, username FROM account;")
        val resultSet = stmt.executeQuery()
        return@withConnection mutableListOf<AccountModel>().apply {
            while (resultSet.next()) {
                add(convertToResultSetToAccountModel(resultSet))
            }
        }
    }

    override suspend fun createAccount(username: String): AccountModel = database.withConnection {
        val stmt =
            it.prepareStatement("INSERT INTO account (username) VALUES ('$username') RETURNING id, username;")
        val resultSet = stmt.executeQuery().apply { this.next() }
        return@withConnection convertToResultSetToAccountModel(resultSet)
    }

    fun convertToResultSetToAccountModel(resultSet: ResultSet) = AccountModel(
        id = resultSet.getInt(1),
        username = resultSet.getString(2)
    )
}
