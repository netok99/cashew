@file:Suppress("MatchingDeclarationName")

package com.wallet

import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

@Resource("/api")
data object RootResource

@Resource("/wallets")
data class WalletsResource(val parent: RootResource = RootResource)

fun Route.walletRoutes(walletService: WalletService) {
    get<WalletsResource> {
        call.respond(status = HttpStatusCode.OK, message = recoverWallets(walletService))
    }
}
