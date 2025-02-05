package com.transaction

import arrow.core.*
import arrow.core.Either.Companion.zipOrAccumulate
import com.tranformation.validMcc
import com.tranformation.validMerchant
import com.tranformation.validTotalAmount
import com.wallet.CategoryBenefits
import com.wallet.Wallet
import com.wallet.getAmountValueFromWalletCategory
import com.wallet.WalletModel
import kotlinx.serialization.Serializable

@Serializable
data class TransactionModel(
    val id: Int? = null,
    val accountId: Int,
    val amount: Double,
    val merchant: String,
    val mcc: String
)

fun validateAndTransformToTransaction(model: TransactionModel): Either<String, Transaction> =
    zipOrAccumulate(
        model.id.right(),
        model.accountId.right(),
        model.amount.validTotalAmount(),
        model.merchant.validMerchant(),
        model.mcc.validMcc(),
        ::TransactionModel
    ).map { validatedModel ->
        Transaction(
            id = validatedModel.id,
            accountId = AccountId(validatedModel.accountId),
            mcc = Mcc(validatedModel.mcc),
            merchant = Merchant(validatedModel.merchant),
            amount = Amount(validatedModel.amount)
        )
    }.mapLeft {
        it.joinToString()
    }

@JvmInline
@Serializable
value class AccountId(val value: Int)

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
    val accountId: AccountId,
    val amount: Amount,
    val merchant: Merchant,
    var mcc: Mcc
) {
    val categoryBenefit: CategoryBenefits = discoverCategoryBenefitsFromMcc(mcc)

    fun validateMcc(newMccValue: String) {
        mcc = if (mcc.value != newMccValue) Mcc(newMccValue) else mcc
    }
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

fun discoverCategoryBenefitsFromMcc(mcc: Mcc) =
    when (mcc.value) {
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

fun operationToTransactionResult(operation: Operation) =
    transactionResultCodeToTransactionResult(
        operation
            .map { TransactionResponse.APPROVED }
            .getOrElse { TransactionResponse.REJECTED }
    )
