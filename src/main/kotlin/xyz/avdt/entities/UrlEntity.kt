package xyz.avdt.entities

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import xyz.avdt.utils.currentLocalDateTime

object UrlTable : Table("urls") {
    val shortCode = long("short_code").uniqueIndex().autoIncrement()
    val redirectUrl = text("redirect_url").uniqueIndex()
    val createdAt = datetime("created_at").clientDefault { currentLocalDateTime() }

    override val primaryKey = PrimaryKey(shortCode)
}

