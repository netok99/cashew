package transaction

import arrow.core.left
import arrow.core.right
import arrow.core.some
import com.transaction.AccountId
import com.transaction.Amount
import com.transaction.Mcc
import com.transaction.Merchant
import com.transaction.Transaction
import com.transaction.TransactionService
import com.transaction.createTransaction
import com.transaction.recoverTransactions
import com.wallet.CategoryBenefits
import com.wallet.WalletModel
import com.wallet.WalletService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class TransactionUseCaseTest {
    private val transactionService = mockk<TransactionService>(relaxed = true)
    private val walletService = mockk<WalletService>(relaxed = true)

    @Test
    fun `get transactions`() = runBlocking {
        val transactions = listOf(
            Transaction(
                id = 1,
                accountId = AccountId(1),
                mcc = Mcc("5811"),
                merchant = Merchant("UBER TRIP SAO PAULO BR"),
                amount = Amount(7.50)
            ),
            Transaction(
                id = 2,
                accountId = AccountId(1),
                mcc = Mcc("5811"),
                merchant = Merchant("UBER TRIP SAO PAULO BR"),
                amount = Amount(15.0)
            )
        ).right()
        coEvery { transactionService.getTransactions() } returns transactions

        assertEquals(expected = transactions, actual = recoverTransactions(transactionService))
    }

    @Test
    fun `get transactions fail`() = runBlocking {
        val transactions = "Error".left()
        coEvery { transactionService.getTransactions() } returns transactions

        assertEquals(expected = transactions, actual = recoverTransactions(transactionService))
    }

    @Test
    fun `create transaction`() = runBlocking {
        val transaction = Transaction(
            id = 1,
            accountId = AccountId(1),
            mcc = Mcc("5811"),
            merchant = Merchant("UBER TRIP SAO PAULO BR"),
            amount = Amount(7.50)
        )
        val walletModel = WalletModel(
            id = 1,
            accountId = 1,
            food = 500.0,
            meal = 500.0,
            cash = 500.0
        )

        coEvery { transactionService.createTransaction(transaction) } returns Unit
        coEvery { walletService.getWallet(1) } returns walletModel
        coEvery { walletService.updateWallet(walletModel.some()) } returns Unit

        val actual = createTransaction(
            walletService = walletService,
            transaction = transaction,
            transactionService = transactionService
        )
        val expected = mapOf(
            CategoryBenefits.MEAL to Amount(492.5),
            CategoryBenefits.FOOD to Amount(500.0),
            CategoryBenefits.CASH to Amount(500.0)
        ).some()

        assertEquals(expected = expected, actual = actual)
    }
}
