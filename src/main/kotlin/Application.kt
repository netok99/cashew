package com

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.account.accountsRoutes
import com.environment.Dependencies
import com.environment.Env
import com.environment.dependencies
import com.transaction.transactionRoutes
import com.wallet.walletRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import java.sql.SQLException
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json

fun main(): Unit = SuspendApp {
    val env = Env()
    resourceScope {
        val dependencies = dependencies(env)
        server(Netty, host = env.http.host, port = env.http.port) {
            app(dependencies)
        }
        awaitCancellation()
    }
}

fun Application.app(dependencies: Dependencies) {
    configure()
    routes(dependencies)
}

fun Application.configure() {
    install(DefaultHeaders)
    install(Resources)
    install(ContentNegotiation) {
        json(
            Json {
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = false
            }
        )
    }
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
        exception<SQLException> { call, cause ->
            call.respondText("Database access error with detail: ${cause.message}")
        }
    }
}

fun Application.routes(dependencies: Dependencies) = routing {
    accountsRoutes(dependencies.accountUseCase)
    transactionRoutes(dependencies.transactionUseCase)
    walletRoutes(dependencies.walletUseCase)
}
