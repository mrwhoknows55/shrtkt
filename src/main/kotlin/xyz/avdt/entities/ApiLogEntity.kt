package xyz.avdt.entities

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import xyz.avdt.utils.currentLocalDateTime

object ApiLogTable : Table("api_logs") {
    val id = long("id").uniqueIndex().autoIncrement()
    val createdAt = datetime("created_at").clientDefault { currentLocalDateTime() }
    val httpMethod = text("http_method")
    val path = text("path")
    val status = integer("status")
    val ipAddress = text("ip_address")
    val userAgent = text("user_agent")

    override val primaryKey = PrimaryKey(UrlTable.id)
}