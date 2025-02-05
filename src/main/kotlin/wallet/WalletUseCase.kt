package com.wallet

import arrow.core.Option

interface WalletService {
    suspend fun getWallets(): List<WalletModel>

    suspend fun getWallet(accountId: Int): WalletModel

    suspend fun createWallet(accountId: Int)

    suspend fun updateWallet(walletToUpdate: Option<WalletModel>)
}

suspend fun recoverWallets(walletService: WalletService) = walletService.getWallets()

suspend fun recoverWallet(accountId: Int, walletService: WalletService): WalletModel =
    walletService.getWallet(accountId)

suspend fun createWallet(accountId: Int, walletService: WalletService) = walletService.createWallet(accountId)

suspend fun updateWallet(walletService: WalletService, walletToUpdate: Option<WalletModel>) =
    walletService.updateWallet(walletToUpdate)
