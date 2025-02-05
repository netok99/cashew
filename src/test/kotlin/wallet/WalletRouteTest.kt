package wallet

import client
import com.wallet.WalletModel
import com.wallet.WalletsResource
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WalletRouteTest {
    @Test
    fun `get accounts`() = testApplication {
        val response = client().get(WalletsResource())
        val expected = WalletModel(
            id = 1,
            accountId = 1,
            food = 500.0,
            meal = 500.0,
            cash = 500.0
        )

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue { response.body<List<WalletModel>>().isNotEmpty() }
        assertTrue { response.body<List<WalletModel>>().contains(expected) }
    }
}
