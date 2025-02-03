@file:Suppress("MatchingDeclarationName")

package com.transaction

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.none
import com.wallet.RootResource
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

@Resource("/transactions")
data class TransactionsResource(val parent: RootResource = RootResource) {

    @Resource("new")
    class New(val parent: TransactionsResource = TransactionsResource())
}

fun Route.transactionRoutes(transactionUseCase: TransactionUseCase) {
    get<TransactionsResource> {
        call.respond(status = HttpStatusCode.OK, message = transactionUseCase.recoverTransactions())
    }

    post<TransactionsResource.New> {
        Either
            .runCatching {
                call
                    .receive<TransactionModel>()
                    .validateAndTransformToTransaction()
                    .map { transactionUseCase.createTransaction(it) }.getOrElse { none() }
            }.map {
                call.respond(status = HttpStatusCode.OK, message = operationToTransactionResult(it))
            }
            .getOrElse {
                call.respond(status = HttpStatusCode.OK, message = unknownTransactionResult)
            }
    }
}
