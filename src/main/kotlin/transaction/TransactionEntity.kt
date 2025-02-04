package com.transaction

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import com.wallet.CategoryBenefits
import com.wallet.Wallet
import com.wallet.getAmountValueFromWalletCategory
import com.wallet.WalletModel
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Account(val value: Int)

@JvmInline
@Serializable
value class Mcc(val value: String)

@JvmInline
@Serializable
value class Amount(val value: Double)

@JvmInline
@Serializable
value class Merchant(val value: String)

@Serializable
data class Transaction(
    val id: Int?,
    val accountId: Account,
    val mcc: Mcc,
    val merchant: Merchant,
    val amount: Amount
) {
    val categoryBenefit: CategoryBenefits = discoverCategoryBenefitsFromMcc(mcc)
}

enum class TransactionResponse(val code: String) {
    REJECTED("51"),
    APPROVED("00"),
    UNKNOWN("07")
}

@Serializable
data class TransactionResult(val code: String)

val transactionResultCodeToTransactionResult = { transactionResultCode: TransactionResponse ->
    TransactionResult(transactionResultCode.code)
}

val unknownTransactionResult = TransactionResult(TransactionResponse.UNKNOWN.code)


private fun discoverCategoryBenefitsFromMcc(mcc: Mcc) = when (mcc.value) {
    "5411", "5412" -> CategoryBenefits.FOOD
    "5811", "5812" -> CategoryBenefits.MEAL
    else -> CategoryBenefits.CASH
}

private fun calculateTransaction(transaction: Transaction, wallet: Wallet): Option<Double> {
    val operationValue = getAmountValueFromWalletCategory(
        category = transaction.categoryBenefit,
        wallet = wallet
    ) - transaction.amount.value
    return if (operationValue >= 0) operationValue.some() else none()
}

typealias Operation = Option<Wallet>

fun makeOperation(transaction: Transaction, wallet: Wallet): Operation =
    calculateTransaction(
        transaction = transaction,
        wallet = wallet
    ).map { operationValue ->
        wallet
            .toMutableMap()
            .apply {
                this[transaction.categoryBenefit] = Amount(operationValue)
            }
            .some()
    }.getOrElse {
        none()
    }

fun operationToWalletModel(walletModel: WalletModel, operation: Operation): Option<WalletModel> =
    operation
        .map { wallet ->
            WalletModel(
                id = walletModel.id,
                accountId = walletModel.accountId,
                food = wallet[CategoryBenefits.FOOD]?.value ?: 0.0,
                meal = wallet[CategoryBenefits.MEAL]?.value ?: 0.0,
                cash = wallet[CategoryBenefits.CASH]?.value ?: 0.0
            )
        }

fun operationToTransactionResult(operation: Operation) = transactionResultCodeToTransactionResult(
    operation
        .map { TransactionResponse.APPROVED }
        .getOrElse { TransactionResponse.REJECTED }
)
