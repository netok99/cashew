@file:Suppress("MatchingDeclarationName")

import com.app
import com.environment.Dependencies
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json

suspend fun withServer(test: suspend HttpClient.(dep: Dependencies) -> Unit) {
    val dependencies = KotestProject.dependencies.get()
    testApplication {
        application { app(dependencies) }
        createClient {
            expectSuccess = false
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = false
                    }
                )
            }
            install(Resources)
        }.use { client -> test(client, dependencies) }
    }
}
