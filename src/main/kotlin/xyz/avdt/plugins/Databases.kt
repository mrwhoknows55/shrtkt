package xyz.avdt.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.avdt.entities.UrlTable
import xyz.avdt.utils.getDatabaseEnv

fun Application.configureDatabases() {
    val port = getDatabaseEnv("port")?.toIntOrNull() ?: 5432
    val host = getDatabaseEnv("host") ?: "localhost"
    val user = getDatabaseEnv("user") ?: "postgres"
    val password = getDatabaseEnv("password") ?: "password"
    val dbName = getDatabaseEnv("name") ?: "postgres"

    Database.connect("jdbc:postgresql://$host:$port/$dbName", driver = "org.postgresql.Driver", user = user, password = password)
    transaction {
        SchemaUtils.create(UrlTable)

        val count = UrlTable.selectAll().count()
        println("UrlTable count -> $count")
    }
}
