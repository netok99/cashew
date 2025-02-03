package com.wallet

import com.transaction.Amount

typealias Wallet = Map<CategoryBenefits, Amount>

enum class CategoryBenefits {
    FOOD,
    MEAL,
    CASH
}

fun getAmountValueFromWalletCategory(category: CategoryBenefits, wallet: Wallet) = wallet[category]?.value ?: 0.0
