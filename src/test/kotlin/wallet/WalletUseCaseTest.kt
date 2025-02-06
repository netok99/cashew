package wallet

import com.wallet.WalletModel
import com.wallet.WalletService
import com.wallet.recoverWallets
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals

class WalletUseCaseTest {
    private val walletService = mockk<WalletService>(relaxed = true)

    @Test
    fun `recover wallets success`() = runBlocking {
        val wallets = listOf(
            WalletModel(
                id = 1,
                accountId = 1,
                food = 500.0,
                meal = 500.0,
                cash = 500.0
            ),
            WalletModel(
                id = 2,
                accountId = 2,
                food = 500.0,
                meal = 500.0,
                cash = 500.0
            )
        )
        coEvery { walletService.getWallets() } returns wallets
        val actual = recoverWallets(walletService)

        assertEquals(expected = wallets, actual = actual)
    }

    @Test
    fun `recover wallets fail`(): Unit = runBlocking {
        coEvery { walletService.getWallets() } throws SQLException("This is a test")

        assertThrows<SQLException> { recoverWallets(walletService) }
    }
}
