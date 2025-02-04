@file:Suppress("MatchingDeclarationName")

package com.transaction

import arrow.core.Either
import com.wallet.walletModelToWallet
import com.wallet.WalletUseCase

interface TransactionService {
    suspend fun getTransactions(): Either<String, List<Transaction>>

    suspend fun getTransaction(accountId: Int): List<TransactionModel>

    suspend fun createTransaction(transaction: Transaction)
}

class TransactionUseCase(
    private val transactionService: TransactionService,
    private val walletUseCase: WalletUseCase
) {
    suspend fun recoverTransactions(): Either<String, List<Transaction>> = transactionService.getTransactions()

    suspend fun createTransaction(transaction: Transaction): Operation {
        val walletModel = walletUseCase.recoverWallet(transaction.accountId.value)
        val wallet = walletModelToWallet(walletModel)
        val operation = makeOperation(transaction = transaction, wallet = wallet)
        transactionService.createTransaction(transaction)
        walletUseCase.updateWallet(
            walletToUpdate = operationToWalletModel(walletModel = walletModel, operation = operation)
        )
        return operation
    }
}
