package com.wallet.entity

typealias Wallet = Map<CategoryBenefits, Amount>

enum class CategoryBenefits {
    FOOD,
    MEAL,
    CASH
}

fun initialWalletSetup(): Wallet = mapOf(
    CategoryBenefits.MEAL to Amount(100.0),
    CategoryBenefits.FOOD to Amount(100.0),
    CategoryBenefits.CASH to Amount(100.0)
)

fun getAmountValueFromWalletCategory(category: CategoryBenefits, wallet: Wallet) = wallet[category]?.value ?: 0.0
