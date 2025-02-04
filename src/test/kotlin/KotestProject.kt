import arrow.fx.coroutines.resource
import com.environment.Env
import com.environment.dependencies
import com.environment.hikari
import io.kotest.assertions.arrow.fx.coroutines.ProjectResource
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.listeners.TestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.testcontainers.StartablePerProjectListener
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

private class PostgresSQL : PostgreSQLContainer<PostgresSQL>("postgres:latest") {
    init {
        waitingFor(Wait.forListeningPort())
    }
}

object KotestProject : AbstractProjectConfig() {
    private val postgres = PostgresSQL()

    private val dataSource: Env.DataSource by lazy {
        Env.DataSource(postgres.jdbcUrl, postgres.username, postgres.password, postgres.driverClassName)
    }

    private val env: Env by lazy { Env().copy(dataSource = dataSource) }

    val dependencies = ProjectResource(resource { dependencies(env) })
    private val hikari = ProjectResource(resource { hikari(env.dataSource) })

    override val globalAssertSoftly: Boolean = true

    private val resetDatabaseListener =
        object : TestListener {
            override suspend fun afterTest(testCase: TestCase, result: TestResult) {
                super.afterTest(testCase, result)
                hikari.get().connection.use { conn ->
                    conn.prepareStatement("TRUNCATE account, wallet, transaction CASCADE").executeLargeUpdate()
                }
            }
        }

    override fun extensions(): List<Extension> =
        listOf(StartablePerProjectListener(postgres), hikari, dependencies, resetDatabaseListener)
}
