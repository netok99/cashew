package transaction

import client
import com.account.AccountModel
import com.account.AccountsResource
import com.transaction.Account
import com.transaction.Amount
import com.transaction.Mcc
import com.transaction.Merchant
import com.transaction.Transaction
import com.transaction.TransactionModel
import com.transaction.TransactionResult
import com.transaction.TransactionsResource
import com.wallet.WalletModel
import com.wallet.WalletsResource
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TransactionRouteTest {
    @Test
    fun `get transactions`() = testApplication {
        val response = client().get(TransactionsResource())

        assertEquals(HttpStatusCode.OK, response.status)
        assertIs<List<AccountModel>>(response.body<List<AccountModel>>())
    }

    @Test
    fun `create transaction success`() = testApplication {
        val client = client()
        val createTransactionResponse = client.post(TransactionsResource.New()) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
                TransactionModel(
                    accountId = 1,
                    amount = 50.0,
                    merchant = "UBER TRIP SAO PAULO BR",
                    mcc = "5811"
                )
            )
        }
        val createTransactionExpected = TransactionResult("00")

        assertEquals(HttpStatusCode.OK, createTransactionResponse.status)
        assertEquals(createTransactionExpected, createTransactionResponse.body())

        val transactionsResponse = client.get(TransactionsResource())
        val transactionExpected = Transaction(
            id = 1,
            accountId = Account(1),
            mcc = Mcc("5811"),
            merchant = Merchant("UBER TRIP SAO PAULO BR"),
            amount = Amount(50.0)
        )

        assertEquals(HttpStatusCode.OK, transactionsResponse.status)
        assertTrue { transactionsResponse.body<List<Transaction>>().contains(transactionExpected) }

        val walletsResponse = client.get(WalletsResource())
        val walletExpected = WalletModel(
                id = 1,
                accountId = 1,
                food = 500.0,
                meal = 450.0,
                cash = 500.0
            )

        assertEquals(HttpStatusCode.OK, walletsResponse.status)
        assertTrue { walletsResponse.body<List<WalletModel>>().contains(walletExpected) }
    }

    @Test
    fun `create existing account fail`() = testApplication {
        val response = client().post(AccountsResource.New()) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(AccountModel(username = "Edson Arantes do Nascimento"))
        }
        assertEquals(HttpStatusCode.BadGateway, response.status)
        assertEquals("Error recovering accounts.", response.bodyAsText())
    }

    @Test
    fun `create existing account fail1`() = testApplication {
        val response = client().post(AccountsResource.New()) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(AccountModel(username = "Edson Arantes do Nascimento"))
        }
        assertEquals(HttpStatusCode.BadGateway, response.status)
        assertEquals("Error recovering accounts.", response.bodyAsText())
    }
}
