package account

import client
import com.account.AccountModel
import com.account.AccountsResource
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
import kotlin.test.assertTrue

class AccountRouteTest {
    @Test
    fun `get accounts`() = testApplication {
        val response = client().get(AccountsResource())
        val expected = listOf(AccountModel(id = 1, username = "Edson Arantes do Nascimento"))

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue { response.body<List<AccountModel>>().isNotEmpty() }
        assertEquals(expected = expected.first(), actual = response.body<List<AccountModel>>().first())
    }

    @Test
    fun `create account success`() = testApplication {
        val client = client()
        val createAccountResponse = client.post(AccountsResource.New()) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(AccountModel(username = "Johan Cruijff"))
        }

        assertEquals(HttpStatusCode.OK, createAccountResponse.status)
        assertEquals("Account and wallet created.", createAccountResponse.bodyAsText())

        val accountsResponse = client.get(AccountsResource())
        val accountsExpected = listOf(
            AccountModel(id = 1, username = "Edson Arantes do Nascimento"),
            AccountModel(id = 2, username = "Johan Cruijff")
        )

        assertEquals(HttpStatusCode.OK, accountsResponse.status)
        assertEquals(accountsExpected, accountsResponse.body())

        val walletsResponse = client.get(WalletsResource())
        val walletsExpected = listOf(
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

        assertEquals(HttpStatusCode.OK, walletsResponse.status)
        assertEquals(walletsExpected, walletsResponse.body())
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
}
