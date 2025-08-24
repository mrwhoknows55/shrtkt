package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import xyz.avdt.entities.UrlTable
import xyz.avdt.utils.currentLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Routing.urlShortenerRoutes() {

    route("/shorten") {
        post {
            val encodedUrl = runCatching { call.receiveText().trim().encodeURLPath() }.getOrElse { "" }
            val longUrl = parseUrl(encodedUrl)?.toString() ?: return@post call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )
            val shortCode = transaction {
                runCatching {
                    UrlTable.select(UrlTable.shortCode).where { UrlTable.redirectUrl eq longUrl }
                        .single()[UrlTable.shortCode]

                }.getOrNull()?.let {
                    return@transaction it
                }
                runCatching {
                    UrlTable.insert {
                        it[redirectUrl] = longUrl
                    } get UrlTable.shortCode
                }.getOrNull()
            }
            shortCode?.let {
                println("added $longUrl on $shortCode code")
                return@post call.respond(HttpStatusCode.Created, mapOf("shortCode" to shortCode))
            }
            return@post call.respondText("URL not found", status = HttpStatusCode.NotFound)
        }

        put {
            call.respondText(
                "wrong request format for short code", status = HttpStatusCode.BadRequest
            )
        }

        put("{shortCode}") {
            val shortCode = call.request.pathVariables["shortCode"]?.toLongOrNull() ?: return@put call.respondText(
                "wrong request format for short code", status = HttpStatusCode.BadRequest
            )
            val encodedUrl = runCatching { call.receiveText().trim().encodeURLPath() }.getOrElse { "" }
            val urlToUpdate = parseUrl(encodedUrl)?.toString() ?: return@put call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )

            val doesNewUrlAlreadyExist = transaction {
                UrlTable.select(UrlTable.redirectUrl).where { UrlTable.redirectUrl eq urlToUpdate }.count() > 0L
            }
            if (doesNewUrlAlreadyExist) {
                return@put call.respond(
                    status = HttpStatusCode.BadRequest, "this url is already shortened"
                )
            }

            val count = transaction {
                UrlTable.update({ UrlTable.shortCode eq shortCode }) {
                    it[redirectUrl] = urlToUpdate
                }
            }

            if (count > 0) {
                println("updated url $urlToUpdate on shortCode: $shortCode")
                return@put call.respond(HttpStatusCode.Accepted, mapOf("shortCode" to shortCode))
            } else {
                return@put call.respondText("URL for the short code doesn't exist", status = HttpStatusCode.NotFound)
            }

        }

        delete {
            call.respondText(
                "wrong request format for short code", status = HttpStatusCode.BadRequest
            )
        }

        delete("/{shortCode}") {
            val shortCode = call.request.pathVariables["shortCode"]?.toLongOrNull() ?: return@delete call.respondText(
                "wrong request format for short code", status = HttpStatusCode.BadRequest
            )

            val result = transaction {
                runCatching {
                    UrlTable.deleteWhere { UrlTable.shortCode eq shortCode }
                }.getOrElse { -1 }
            }

            if (result > 0) {
                println("shortCode: $shortCode deleted")
                call.respondText("short code deleted successfully", status = HttpStatusCode.NoContent)
            } else {
                println("shortCode: $shortCode not found")
                call.respondText("short code not found", status = HttpStatusCode.NotFound)
            }

        }
    }

    get("/redirect") {
        val code = call.request.queryParameters["code"]?.toLongOrNull() ?: return@get call.respondText(
            "wrong request format for short code", status = HttpStatusCode.BadRequest
        )
        val result = transaction {
            runCatching {
                val longUrl = UrlTable.select(UrlTable.redirectUrl).where { UrlTable.shortCode eq code }
                    .single()[UrlTable.redirectUrl]
                return@transaction longUrl
            }.getOrNull()
        }
        result?.let {
            runCatching {
                transaction {
                    UrlTable.update({
                        UrlTable.shortCode eq code
                    }) {
                        it[UrlTable.visitCount] = UrlTable.visitCount + 1
                        it[UrlTable.lastAccessedAt] = currentLocalDateTime()
                    }
                }
            }
            return@get call.respondRedirect(it)
        }
        return@get call.respondText("URL not found", status = HttpStatusCode.NotFound)
    }

}