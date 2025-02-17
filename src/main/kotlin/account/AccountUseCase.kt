package com.account

import arrow.core.Either
import com.wallet.WalletService
import com.wallet.createWallet

interface AccountService {
    suspend fun getAccounts(): List<Account>

    suspend fun createAccount(username: String): Account
}

suspend fun recoverAccounts(accountService: AccountService): List<Account> = accountService.getAccounts()

suspend fun createAccount(
    accountService: AccountService,
    walletService: WalletService,
    username: String
) = Either
    .catch {
        accountService.createAccount(username).id?.let {
            createWallet(accountId = it, walletService = walletService)
        }
    }.mapLeft { it.message }
