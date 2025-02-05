package transaction

import arrow.core.none
import arrow.core.some
import com.transaction.Account
import com.transaction.Amount
import com.transaction.Mcc
import com.transaction.Merchant
import com.transaction.Operation
import com.transaction.Transaction
import com.transaction.TransactionResult
import com.transaction.discoverCategoryBenefitsFromMcc
import com.transaction.makeOperation
import com.transaction.operationToTransactionResult
import com.transaction.operationToWalletModel
import com.transaction.unknownTransactionResult
import com.wallet.CategoryBenefits
import com.wallet.WalletModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionEntityTest {
    @Test
    fun `discover category benefits from Mcc`() {
        assertEquals(
            expected = discoverCategoryBenefitsFromMcc(Mcc("5411")),
            actual = CategoryBenefits.FOOD
        )
        assertEquals(
            expected = discoverCategoryBenefitsFromMcc(Mcc("5412")),
            actual = CategoryBenefits.FOOD
        )
        assertEquals(
            expected = discoverCategoryBenefitsFromMcc(Mcc("5811")),
            actual = CategoryBenefits.MEAL
        )
        assertEquals(
            expected = discoverCategoryBenefitsFromMcc(Mcc("5812")),
            actual = CategoryBenefits.MEAL
        )
        assertEquals(
            expected = discoverCategoryBenefitsFromMcc(Mcc("8049")),
            actual = CategoryBenefits.CASH
        )
    }

    @Test
    fun `make operation success`() {
        val operation = makeOperation(
            transaction = Transaction(
                id = 1,
                accountId = Account(1),
                mcc = Mcc("5811"),
                merchant = Merchant("UBER TRIP SAO PAULO BR"),
                amount = Amount(50.0)
            ),
            wallet = mapOf(
                CategoryBenefits.MEAL to Amount(500.0),
                CategoryBenefits.FOOD to Amount(500.0),
                CategoryBenefits.CASH to Amount(500.0)
            )
        )

        assertTrue { operation.isSome() }
        assertEquals(
            expected = mapOf(
                CategoryBenefits.MEAL to Amount(450.0),
                CategoryBenefits.FOOD to Amount(500.0),
                CategoryBenefits.CASH to Amount(500.0)
            ).some(),
            actual = operation.map { it }
        )
    }

    @Test
    fun `make operation fail`() {
        val operation = makeOperation(
            transaction = Transaction(
                id = 1,
                accountId = Account(1),
                mcc = Mcc("5811"),
                merchant = Merchant("UBER TRIP SAO PAULO BR"),
                amount = Amount(600.0)
            ),
            wallet = mapOf(
                CategoryBenefits.MEAL to Amount(500.0),
                CategoryBenefits.FOOD to Amount(500.0),
                CategoryBenefits.CASH to Amount(500.0)
            )
        )

        assertTrue { operation.isNone() }
    }

    @Test
    fun `operation to WalletModel`() {
        val walletModel = WalletModel(
            id = 1,
            accountId = 1,
            food = 500.0,
            meal = 500.0,
            cash = 500.0
        )
        val operation: Operation = mapOf(
            CategoryBenefits.MEAL to Amount(300.0),
            CategoryBenefits.FOOD to Amount(300.0),
            CategoryBenefits.CASH to Amount(250.0)
        ).some()

        val actual = operationToWalletModel(walletModel, operation)
        val expected = WalletModel(
            id = 1,
            accountId = 1,
            food = 300.0,
            meal = 300.0,
            cash = 250.0
        ).some()

        assertEquals(expected, actual)
    }

    @Test
    fun `operation to TransactionResult`() {
        val operation: Operation = mapOf(
            CategoryBenefits.MEAL to Amount(300.0),
            CategoryBenefits.FOOD to Amount(300.0),
            CategoryBenefits.CASH to Amount(250.0)
        ).some()

        assertEquals(
            expected = TransactionResult("00"),
            actual = operationToTransactionResult(operation)
        )

        val secondOperation: Operation = none()

        assertEquals(
            expected = operationToTransactionResult(secondOperation),
            actual = TransactionResult("51")
        )

        assertEquals(
            expected = unknownTransactionResult,
            actual = TransactionResult("07")
        )
    }
}
