package com


import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import com.env.Dependencies
import com.env.Env
import com.env.dependencies
import com.sksamuel.cohort.Cohort
import com.sksamuel.cohort.HealthCheckRegistry
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
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
    health(dependencies.healthCheck)
}

fun Application.configure() {
    install(DefaultHeaders)
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
    get("/") {
        call.respondText("Hello World!")
    }
}

fun Application.health(healthCheck: HealthCheckRegistry) {
    install(Cohort) { healthcheck("/readiness", healthCheck) }
}
