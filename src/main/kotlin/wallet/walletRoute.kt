@file:Suppress("MatchingDeclarationName")

package com.wallet

import arrow.core.Either
import com.environment.Dependencies
import com.mutableWallet
import com.wallet.entity.changeStateFromMutableWallet
import com.wallet.entity.makeOperation
import com.wallet.entity.operationToTransactionResult
import com.wallet.entity.unknownTransactionResult
import com.wallet.model.TransactionModel
import com.wallet.model.validateAndTransformToTransaction
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

@Resource("/api")
data object RootResource

@Resource("/charges")
data class ChargesResource(val parent: RootResource = RootResource) {
    @Resource("wallet")
    data class Wallet(val parent: ChargesResource = ChargesResource())
}

fun Route.walletRoutes(dependencies: Dependencies) {
    get<ChargesResource.Wallet> {
        call.respond(status = HttpStatusCode.OK, message = mutableWallet.toString())
    }

    post<ChargesResource.Wallet> {
        Either
            .runCatching {
                call.receive<TransactionModel>()
            }
            .map { transactionModel ->
                val operation = makeOperation(
                    transaction = transactionModel.validateAndTransformToTransaction(),
                    wallet = mutableWallet
                )
                changeStateFromMutableWallet(
                    operation = operation,
                    wallet = mutableWallet
                )
                call.respond(status = HttpStatusCode.OK, message = operationToTransactionResult(operation))
            }
            .getOrElse {
                call.respond(status = HttpStatusCode.OK, message = unknownTransactionResult)
            }
    }
}
