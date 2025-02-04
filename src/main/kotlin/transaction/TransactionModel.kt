package com.transaction

import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import arrow.core.right
import com.tranformation.validMcc
import com.tranformation.validMerchant
import com.tranformation.validTotalAmount
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
            accountId = Account(validatedModel.accountId),
            mcc = Mcc(validatedModel.mcc),
            merchant = Merchant(validatedModel.merchant),
            amount = Amount(validatedModel.amount)
        )
    }.mapLeft {
        it.joinToString()
    }
