package account

import arrow.core.right
import com.account.Account
import com.account.AccountService
import com.account.createAccount
import com.account.recoverAccounts
import com.wallet.WalletService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AccountUseCaseTest {

    private val accountService = mockk<AccountService>(relaxed = true)
    private val walletService = mockk<WalletService>(relaxed = true)

    @Test
    fun `get accounts`() = runBlocking {
        val accounts = listOf(
            Account(id = 1, username = "Edson Arantes do Nascimento"),
            Account(id = 2, username = "Johan Cruijff")
        )
        coEvery { accountService.getAccounts() } returns accounts

        assertEquals(expected = accounts, actual = recoverAccounts(accountService))
    }

    @Test
    fun `create account`() = runBlocking {
        val username = "Johan Cruijff"
        coEvery { accountService.createAccount(username) } returns Account(id = 2, username = username)
        val actual = createAccount(
            accountService = accountService,
            walletService = walletService,
            username = username
        )
        val expected = Unit.right()

        assertEquals(expected = expected, actual = actual)
    }
}
