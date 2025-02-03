package com

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.environment.Env
import com.environment.Dependencies
import com.environment.dependencies
import com.wallet.entity.initialWalletSetup
import com.wallet.walletRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.json.Json

val wallet = initialWalletSetup()

var mutableWallet = wallet.toMutableMap()

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
            }
        )
    }
}

fun Application.routes(dependencies: Dependencies) = routing {
    walletRoutes(dependencies)
}
