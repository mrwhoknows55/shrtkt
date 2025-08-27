package xyz.avdt.entities

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import xyz.avdt.utils.currentLocalDateTime

object UserTable : Table("users") {
    val id = long("id").uniqueIndex().autoIncrement() // set it to autoincrement, this is your primary key
    val email = text("email").uniqueIndex()
    val name = text("name").nullable()
    val apiKey = text("api_key")
    val createdAt = datetime("created_at").default(currentLocalDateTime()).clientDefault { currentLocalDateTime() }

    override val primaryKey = PrimaryKey(id)
}
