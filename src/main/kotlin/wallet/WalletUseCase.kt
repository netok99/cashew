@file:Suppress("MatchingDeclarationName")

package com.wallet

import arrow.core.Option

interface WalletService {
    suspend fun getWallets(): List<WalletModel>

    suspend fun getWallet(accountId: Int): WalletModel

    suspend fun createWallet(accountId: Int)

    suspend fun updateWallet(walletToUpdate: Option<WalletModel>)
}

class WalletUseCase(private val walletService: WalletService) {

    suspend fun recoverWallets() = walletService.getWallets()

    suspend fun recoverWallet(accountId: Int): WalletModel = walletService.getWallet(accountId)

    suspend fun createWallet(accountId: Int) = walletService.createWallet(accountId)

    suspend fun updateWallet(walletToUpdate: Option<WalletModel>) = walletService.updateWallet(walletToUpdate)
}
