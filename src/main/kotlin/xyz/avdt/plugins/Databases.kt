package xyz.avdt.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils
import xyz.avdt.entities.UrlTable
import xyz.avdt.entities.UserTable
import xyz.avdt.utils.getDatabaseEnv

fun Application.configureDatabases() {
    val port = getDatabaseEnv("port")?.toIntOrNull() ?: 5432
    val host = getDatabaseEnv("host") ?: "localhost"
    val user = getDatabaseEnv("user") ?: "postgres"
    val password = getDatabaseEnv("password") ?: "password"
    val dbName = getDatabaseEnv("name") ?: "postgres"

    Database.connect(
        "jdbc:postgresql://$host:$port/$dbName", driver = "org.postgresql.Driver", user = user, password = password
    )
    transaction {
        SchemaUtils.create(UrlTable)
        val statements = MigrationUtils.statementsRequiredForDatabaseMigration(UrlTable, UserTable)
        transaction {
            runCatching {
                exec("ALTER TABLE urls DROP CONSTRAINT IF EXISTS urls_redirect_url_unique")
                exec("DROP INDEX IF EXISTS urls_redirect_url_unique")
                println("Dropped unique index on redirect_url")
            }.onFailure {
                it.printStackTrace()
            }
            runCatching {
                statements.forEach {
                    println("executing migration statement: $it")
                    exec(it)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }

        val count = UrlTable.selectAll().count()
        println("UrlTable count -> $count")
    }
}
