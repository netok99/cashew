package com.transaction

import arrow.core.Either
import com.wallet.WalletService
import com.wallet.recoverWallet
import com.wallet.updateWallet
import com.wallet.walletModelToWallet

interface TransactionService {
    suspend fun getTransactions(): Either<String, List<Transaction>>

    suspend fun createTransaction(transaction: Transaction)

    suspend fun getMcc(transaction: Transaction): String
}

suspend fun recoverTransactions(transactionService: TransactionService) = transactionService.getTransactions()

suspend fun createTransaction(
    transaction: Transaction,
    transactionService: TransactionService,
    walletService: WalletService
): Operation {
    val walletModel = recoverWallet(accountId = transaction.accountId.value, walletService = walletService)
    val wallet = walletModelToWallet(walletModel)
    transaction.validateMcc(transactionService.getMcc(transaction))
    val operation = makeOperation(transaction = transaction, wallet = wallet)
    transactionService.createTransaction(transaction)
    updateWallet(
        walletToUpdate = operationToWalletModel(walletModel = walletModel, operation = operation),
        walletService = walletService
    )
    return operation
}
