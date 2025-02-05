package com.wallet

import com.transaction.Amount
import kotlinx.serialization.Serializable

@Serializable
data class WalletModel(
    val id: Int,
    val accountId: Int,
    val food: Double,
    val meal: Double,
    val cash: Double
)

fun walletModelToWallet(model: WalletModel): Wallet = mapOf(
    CategoryBenefits.MEAL to Amount(model.meal),
    CategoryBenefits.FOOD to Amount(model.food),
    CategoryBenefits.CASH to Amount(model.cash)
)

typealias Wallet = Map<CategoryBenefits, Amount>

enum class CategoryBenefits {
    FOOD,
    MEAL,
    CASH
}

fun getAmountValueFromWalletCategory(category: CategoryBenefits, wallet: Wallet) = wallet[category]?.value ?: 0.0
