package com.wallet.entity

import kotlinx.serialization.Serializable

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
