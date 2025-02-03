@file:Suppress("MatchingDeclarationName")

package com.account

import arrow.core.Either
import com.wallet.RootResource
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

const val ERROR_RECOVER_ACCOUNTS_MESSAGE = "Error recovering accounts."
const val SUCCESS_CREATE_ACCOUNT_WALLET_MESSAGE = "Account and wallet created."

@Resource("/accounts")
data class AccountsResource(val parent: RootResource = RootResource) {
    @Resource("new")
    class New(val parent: AccountsResource = AccountsResource())
}

fun Route.accountsRoutes(accountUseCase: AccountUseCase) {
    get<AccountsResource> {
        Either
            .runCatching { accountUseCase.recoverAccounts() }
            .map { accountModel -> call.respond(status = HttpStatusCode.OK, message = accountModel) }
            .getOrElse { call.respond(status = HttpStatusCode.OK, message = ERROR_RECOVER_ACCOUNTS_MESSAGE) }
    }

    post<AccountsResource.New> {
        Either
            .runCatching { accountUseCase.createAccount(call.receive<AccountModel>().username) }
            .map { call.respond(status = HttpStatusCode.OK, message = SUCCESS_CREATE_ACCOUNT_WALLET_MESSAGE) }
            .getOrElse { call.respond(status = HttpStatusCode.OK, message = ERROR_RECOVER_ACCOUNTS_MESSAGE) }
    }
}
