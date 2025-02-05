@file:Suppress("MatchingDeclarationName")

package com.transaction

import arrow.core.Either
import com.wallet.WalletService
import com.wallet.walletModelToWallet
import com.wallet.recoverWallet
import com.wallet.updateWallet

interface TransactionService {
    suspend fun getTransactions(): Either<String, List<Transaction>>

    suspend fun getTransaction(accountId: Int): List<TransactionModel>

    suspend fun createTransaction(transaction: Transaction)
}


suspend fun recoverTransactions(transactionService: TransactionService) = transactionService.getTransactions()

suspend fun createTransaction(
    transaction: Transaction,
    transactionService: TransactionService,
    walletService: WalletService
): Operation {
    val walletModel = recoverWallet(
        accountId = transaction.accountId.value,
        walletService = walletService
    )
    val wallet = walletModelToWallet(walletModel)
    val operation = makeOperation(transaction = transaction, wallet = wallet)
    transactionService.createTransaction(transaction)
    updateWallet(
        walletToUpdate = operationToWalletModel(walletModel = walletModel, operation = operation),
        walletService = walletService
    )
    return operation
}
