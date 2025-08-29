package xyz.avdt.entities

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime
import xyz.avdt.utils.currentLocalDateTime

object UrlTable : Table("urls") {
    val shortCode = long("short_code").uniqueIndex().autoIncrement()
    val redirectUrl = text("redirect_url")
    val visitCount = long("visit_count").default(0)
    val createdAt = datetime("created_at").clientDefault { currentLocalDateTime() }
    val lastAccessedAt = datetime("last_accessed_at").nullable().default(null).clientDefault { currentLocalDateTime() }
    val userId = reference("user_id", UserTable.id).nullable().default(null)
    val deletedAt = datetime("deleted_at").nullable().default(null)
    val expiredAt = datetime("expired_at").nullable().default(null)

    override val primaryKey = PrimaryKey(shortCode)
}

