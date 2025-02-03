package com.wallet

import com.environment.Database

interface WalletService {
    suspend fun makeTransaction(): String

    suspend fun getWallet(): String

    suspend fun getTransactionHistory(): String
}

fun walletService(database: Database): WalletService = object : WalletService {
    override suspend fun makeTransaction(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getWallet(): String {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionHistory(): String {
        TODO("Not yet implemented")
    }
}
