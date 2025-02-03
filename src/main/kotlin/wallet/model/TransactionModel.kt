package com.wallet.model

import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import com.tranformation.validAccount
import com.tranformation.validMcc
import com.tranformation.validMerchant
import com.tranformation.validTotalAmount
import com.wallet.entity.Account
import com.wallet.entity.Amount
import com.wallet.entity.Mcc
import com.wallet.entity.Merchant
import com.wallet.entity.Transaction
import kotlinx.serialization.Serializable

@Serializable
data class TransactionModel(
    val account: String,
    val mcc: String,
    val merchant: String,
    val totalAmount: Double
)

fun TransactionModel.validateAndTransformToTransaction(): Either<String, Transaction> =
    zipOrAccumulate(
        mcc.validMcc(),
        account.validAccount(),
        merchant.validMerchant(),
        totalAmount.validTotalAmount(),
        ::TransactionModel
    ).map { validatedModel ->
        Transaction(
            account = Account(validatedModel.account),
            mcc = Mcc(validatedModel.mcc),
            merchant = Merchant(validatedModel.merchant),
            totalAmount = Amount(validatedModel.totalAmount)
        )
    }.mapLeft {
        it.joinToString()
    }
