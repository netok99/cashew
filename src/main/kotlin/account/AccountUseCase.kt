package com.account

import arrow.core.Either
import com.wallet.WalletUseCase

interface AccountService {
    suspend fun getAccounts(): List<AccountModel>

    suspend fun createAccount(username: String): AccountModel
}

class AccountUseCase(
    private val accountService: AccountService,
    private val walletUseCase: WalletUseCase
) {
    suspend fun recoverAccounts(): List<AccountModel> = accountService.getAccounts()

    suspend fun createAccount(username: String) = Either
        .runCatching {
            accountService.createAccount(username).id?.let {
                walletUseCase.createWallet(it)
            }
        }
}