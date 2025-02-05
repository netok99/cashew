package com.wallet

import arrow.core.Option
import com.environment.Database
import java.sql.ResultSet

private const val ID_PARAMETER = "{id}"
private const val ACCOUNT_ID_PARAMETER = "{accountId}"
private const val FOOD_PARAMETER = "{food}"
private const val MEAL_PARAMETER = "{meal}"
private const val CASH_PARAMETER = "{cash}"
private const val GET_WALLETS_QUERY = "SELECT id, account_id, food, meal, cash FROM wallet ORDER BY id;"
private const val GET_WALLET_QUERY =
    """SELECT id, account_id, food, meal, cash FROM wallet WHERE "account_id" = $ACCOUNT_ID_PARAMETER"""
private const val CREATE_WALLET_QUERY = "INSERT INTO wallet (account_id, food, meal, cash) VALUES " +
        "($ACCOUNT_ID_PARAMETER, 500.0, 500.0, 500.0) RETURNING id;"
private const val UPDATE_WALLET_QUERY = "UPDATE wallet SET food = $FOOD_PARAMETER, meal = $MEAL_PARAMETER, " +
        "cash = $CASH_PARAMETER WHERE id = $ID_PARAMETER AND account_id = $ACCOUNT_ID_PARAMETER RETURNING id;"

fun walletService(database: Database): WalletService = object : WalletService {

    override suspend fun getWallets(): List<WalletModel> = database.withConnection {
        val prepareStatement = it.prepareStatement(GET_WALLETS_QUERY)
        val resultSet = prepareStatement.executeQuery()
        return@withConnection mutableListOf<WalletModel>().apply {
            while (resultSet.next()) {
                add(convertResultSetToWalletModel(resultSet))
            }
        }
    }

    override suspend fun getWallet(accountId: Int): WalletModel = database.withConnection {
        val prepareStatement = it.prepareStatement(GET_WALLET_QUERY.replace(ACCOUNT_ID_PARAMETER, accountId.toString()))
        return@withConnection convertResultSetToWalletModel(
            resultSet = prepareStatement.executeQuery().apply { next() }
        )
    }

    override suspend fun createWallet(accountId: Int) {
        database.withConnection {
            val prepareStatement =
                it.prepareStatement(CREATE_WALLET_QUERY.replace(ACCOUNT_ID_PARAMETER, accountId.toString()))
            prepareStatement.executeQuery()
        }
    }

    override suspend fun updateWallet(walletToUpdate: Option<WalletModel>) {
        walletToUpdate.map { walletModel ->
            database.withConnection {
                val prepareStatement = it.prepareStatement(
                    UPDATE_WALLET_QUERY
                        .replace(ID_PARAMETER, walletModel.id.toString())
                        .replace(ACCOUNT_ID_PARAMETER, walletModel.accountId.toString())
                        .replace(FOOD_PARAMETER, walletModel.food.toString())
                        .replace(MEAL_PARAMETER, walletModel.meal.toString())
                        .replace(CASH_PARAMETER, walletModel.cash.toString())
                )
                prepareStatement.executeQuery()
            }
        }
    }

    private fun convertResultSetToWalletModel(resultSet: ResultSet) = WalletModel(
        id = resultSet.getInt(1),
        accountId = resultSet.getInt(2),
        food = resultSet.getDouble(3),
        meal = resultSet.getDouble(4),
        cash = resultSet.getDouble(5)
    )
}
