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
                println("Seeding sample users")
                val aliceExists = transaction {
                    UserTable.selectAll().where{
                        UserTable.email eq "alice@email.com"
                    }.count()
                } > 0L
                if (!aliceExists) {
                    println("Inserting Alice test user")
                    exec("INSERT INTO users (email, name, api_key, created_at) VALUES ('alice@email.com', 'Alice', 'sk_test_alice', NOW())")
                } else {
                    println("Alice test user already exists")
                }
                val bobExists = transaction {
                    UserTable.selectAll().where{
                        UserTable.email eq "bob@email.com"
                    }.count()
                } > 0L
                if (!bobExists) {
                    println("Inserting Bob Enterprise test user")
                    exec("INSERT INTO users (email, name, api_key, tier, created_at) VALUES ('bob@email.com', 'Bob Enterprise', 'sk_test_bob', 'ENTERPRISE', NOW())")
                } else {
                    println("Updating Bob Enterprise test user tier")
                    val updateResult = exec("UPDATE users SET tier = 'ENTERPRISE' WHERE email = 'bob@email.com'")
                    println("Update result for Bob Enterprise tier: $updateResult")
                }
                println("added sample users with test API keys")
            }.onFailure {
                println(it.message.orEmpty())
            }
        }

       val count = UrlTable.selectAll().count()
       println("UrlTable count -> $count")
    }
}
