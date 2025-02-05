package transaction

import arrow.core.left
import arrow.core.right
import com.transaction.Account
import com.transaction.Amount
import com.transaction.Mcc
import com.transaction.Merchant
import com.transaction.Transaction
import com.transaction.TransactionModel
import com.transaction.validateAndTransformToTransaction
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionModelTest {
    @Test
    fun `validate and transform to transaction success`() {
        val model = TransactionModel(
            id = 1,
            accountId = 1,
            amount = 50.0,
            merchant = "UBER TRIP SAO PAULO BR",
            mcc = "5411"
        )
        val actual = validateAndTransformToTransaction(model)
        val expected = Transaction(
            id = 1,
            accountId = Account(1),
            amount = Amount(50.0),
            mcc = Mcc("5411"),
            merchant = Merchant("UBER TRIP SAO PAULO BR")
        ).right()

        assertEquals(expected, actual)
    }

    @Test
    fun `validate and transform to transaction validTotalAmount fail`() {
        val model = TransactionModel(
            id = 1,
            accountId = 1,
            amount = -16.0,
            merchant = "UBER TRIP SAO PAULO BR",
            mcc = "5411"
        )
        val actual = validateAndTransformToTransaction(model)
        val expected = "InvalidTotalAmount(errors=NonEmptyList(Cannot be blank or less than 0))".left()

        assertEquals(expected, actual)
    }

    @Test
    fun `validate and transform to transaction merchant fail`() {
        val model = TransactionModel(
            id = 1,
            accountId = 1,
            amount = 50.0,
            merchant = "",
            mcc = "5411"
        )
        val actual = validateAndTransformToTransaction(model)
        val expected = "InvalidMerchant(errors=NonEmptyList(Cannot be blank))".left()

        assertEquals(expected, actual)
    }

    @Test
    fun `validate and transform to transaction mcc fail`() {
        val model = TransactionModel(
            id = 1,
            accountId = 1,
            amount = 50.0,
            merchant = "UBER TRIP SAO PAULO BR",
            mcc = "541"
        )
        val actual = validateAndTransformToTransaction(model)
        val expected = "InvalidMcc(errors=NonEmptyList(Mcc: ${model.mcc} is invalid))".left()

        assertEquals(expected, actual)
    }
}
