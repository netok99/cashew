package com

import com.account.accountsRoutes
import com.environment.Dependencies
import com.environment.Env
import com.environment.dependencies
import com.transaction.transactionRoutes
import com.wallet.walletRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.sql.SQLException

fun main() {
    val env = Env()
    embeddedServer(
        Netty,
        host = env.http.host,
        port = env.http.port,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configure()
    routes(dependencies(Env()))
}

private const val ILLEGAL_STATE_EXCEPTION_MESSAGE = "App in illegal state as {message}"
private const val SQL_EXCEPTION_MESSAGE = "Database access error with detail: {message}"
private const val MESSAGE_PARAMETER = "{message}"

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
            call.respondText(ILLEGAL_STATE_EXCEPTION_MESSAGE.replace(MESSAGE_PARAMETER, cause.message.toString()))
        }
        exception<SQLException> { call, cause ->
            call.respondText(SQL_EXCEPTION_MESSAGE.replace(MESSAGE_PARAMETER, cause.message.toString()))
        }
    }
}

fun Application.routes(dependencies: Dependencies) = routing {
    accountsRoutes(dependencies.accountService, dependencies.walletService)
    transactionRoutes(dependencies.transactionService, dependencies.walletService)
    walletRoutes(dependencies.walletService)
}
