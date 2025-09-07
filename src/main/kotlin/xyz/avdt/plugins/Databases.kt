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
        val deleteStatements = MigrationUtils.dropUnmappedColumnsStatements(UrlTable, UserTable)
        transaction {
            (deleteStatements + statements).forEach {
                runCatching {
                    println("executing migration statement: $it")
                    exec(it)
                }.onFailure {
                    it.printStackTrace()
                }
            }

            runCatching {
                val rows = UserTable.selectAll().count()
                println("Existing users: $rows")
                if (rows == 0L) {
                    println("Seeding sample users")
                    exec("INSERT INTO users (email, name, api_key, created_at) VALUES ('alice@email.com', 'Alice', 'sk_test_alice', NOW()) ON CONFLICT DO NOTHING")
                    exec("INSERT INTO users (email, name, api_key, created_at) VALUES ('bob@email.com', 'Bob', 'sk_test_bob', NOW()) ON CONFLICT DO NOTHING")
                    println("added sample users with test API keys")
                }
            }.onFailure {
                println(it.message.orEmpty())
            }
        }

        val count = UrlTable.selectAll().count()
        println("UrlTable count -> $count")
    }
}
