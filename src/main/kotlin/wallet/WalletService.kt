package com.wallet

import arrow.core.Option
import com.environment.Database
import java.sql.ResultSet

fun walletService(database: Database): WalletService = object : WalletService {

    override suspend fun getWallets(): List<WalletModel> = database.withConnection {
        val stmt = it.prepareStatement("SELECT id, account_id, food, meal, cash FROM wallet")
        val resultSet = stmt.executeQuery()
        return@withConnection mutableListOf<WalletModel>().apply {
            while (resultSet.next()) {
                add(convertResultSetToWalletModel(resultSet))
            }
        }
    }

    override suspend fun getWallet(accountId: Int): WalletModel = database.withConnection {
        val stmt = it.prepareStatement(
            """SELECT id, account_id, food, meal, cash FROM wallet WHERE "account_id" = $accountId"""
        )
        return@withConnection convertResultSetToWalletModel(resultSet = stmt.executeQuery().apply { next() })
    }

    override suspend fun createWallet(accountId: Int) {
        database.withConnection {
            val stmt = it.prepareStatement(
                "INSERT INTO wallet (account_id, food, meal, cash) VALUES ($accountId, 500.0, 500.0, 500.0) " +
                        "RETURNING id;"
            )
            stmt.executeQuery()
        }
    }

    override suspend fun updateWallet(walletToUpdate: Option<WalletModel>) {
        walletToUpdate.map { walletModel ->
            database.withConnection {
                val stmt = it.prepareStatement(
                    "UPDATE wallet SET food = ${walletModel.food}, meal = ${walletModel.meal}, " +
                            "cash = ${walletModel.cash} WHERE id = ${walletModel.id} AND " +
                            "account_id = ${walletModel.accountId} RETURNING id;"
                )
                stmt.executeQuery()
            }
        }
    }

    fun convertResultSetToWalletModel(resultSet: ResultSet) = WalletModel(
        id = resultSet.getInt(1),
        accountId = resultSet.getInt(2),
        food = resultSet.getDouble(3),
        meal = resultSet.getDouble(4),
        cash = resultSet.getDouble(5)
    )
}
