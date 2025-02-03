package com.wallet.entity

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import io.netty.util.concurrent.FastThreadLocal.removeAll

@JvmInline
value class Account(val value: String)

@JvmInline
value class Mcc(val value: String)

@JvmInline
value class Amount(val value: Double)

@JvmInline
value class Merchant(val value: String)

data class Transaction(
    val account: Account,
    val mcc: Mcc,
    val merchant: Merchant,
    val totalAmount: Amount
) {
    val categoryBenefit: CategoryBenefits = discoverCategoryBenefitsFromMcc(mcc)
}

private fun discoverCategoryBenefitsFromMcc(mcc: Mcc) = when (mcc.value) {
    "5411", "5412" -> CategoryBenefits.FOOD
    "5811", "5812" -> CategoryBenefits.MEAL
    else -> CategoryBenefits.CASH
}

private fun calculateTransaction(transaction: Transaction, wallet: Wallet): Option<Double> {
    val operationValue = getAmountValueFromWalletCategory(
        category = transaction.categoryBenefit,
        wallet = wallet
    ) - transaction.totalAmount.value
    return if (operationValue >= 0) operationValue.some() else none()
}

typealias Operation = Option<Wallet>

fun makeOperation(transaction: Either<String, Transaction>, wallet: Wallet): Operation =
    transaction
        .map {
            calculateTransaction(transaction = it, wallet = wallet)
                .map { operationValue ->
                    wallet
                        .toMutableMap()
                        .apply {
                            this[it.categoryBenefit] = Amount(operationValue)
                        }
                }
        }
        .getOrElse {
            none()
        }

fun changeStateFromMutableWallet(operation: Operation, wallet: MutableMap<CategoryBenefits, Amount>) {
    operation
        .map {
            wallet.run {
                removeAll()
                putAll(it)
            }
        }
}

fun operationToTransactionResult(operation: Operation) = transactionResultCodeToTransactionResult(
    operation
        .map { TransactionResponse.APPROVED }
        .getOrElse { TransactionResponse.REJECTED }
)
