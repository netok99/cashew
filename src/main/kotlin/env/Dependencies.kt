package com.env

import arrow.fx.coroutines.ResourceScope
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.hikari.HikariConnectionsHealthCheck
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers

class Dependencies(
    val healthCheck: HealthCheckRegistry
)

suspend fun ResourceScope.dependencies(env: Env): Dependencies {
    val hikari = hikari(env.dataSource)
    return Dependencies(
        healthCheck = HealthCheckRegistry(Dispatchers.Default) {
            register(HikariConnectionsHealthCheck(hikari, 1), 5.seconds, 5.seconds)
        }
    )
}
