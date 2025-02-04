package account

import com.account.AccountsResource
import io.kotest.core.spec.style.StringSpec
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import withServer

class AccountRouteTest: StringSpec({

    "test" {
        withServer {
            val response = get(AccountsResource())
            assert(response.status == HttpStatusCode.OK)
        }
    }
})
