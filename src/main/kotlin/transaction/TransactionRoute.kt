package com.transaction

import arrow.core.getOrElse
import com.wallet.RootResource
import com.wallet.WalletService
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

private const val RECOVER_TRANSACTIONS_ERROR_MESSAGE = "Error recovering transactions."

@Resource("/transactions")
data class TransactionsResource(val parent: RootResource = RootResource) {
    @Resource("new")
    class New(val parent: TransactionsResource = TransactionsResource())
}

fun Route.transactionRoutes(
    transactionService: TransactionService,
    walletService: WalletService
) {
    get<TransactionsResource> {
        recoverTransactions(transactionService)
            .map { transactions ->
                call.respond(status = HttpStatusCode.OK, message = transactions)
            }
            .getOrElse {
                call.respond(status = HttpStatusCode.BadGateway, message = RECOVER_TRANSACTIONS_ERROR_MESSAGE)
            }
    }

    post<TransactionsResource.New> {
        validateAndTransformToTransaction(call.receive<TransactionModel>())
            .map { transaction ->
                createTransaction(
                    transaction = transaction,
                    transactionService = transactionService,
                    walletService = walletService
                )
            }
            .map {
                call.respond(status = HttpStatusCode.OK, message = operationToTransactionResult(it))
            }
            .getOrElse {
                call.respond(status = HttpStatusCode.OK, message = unknownTransactionResult)
            }
    }
}
