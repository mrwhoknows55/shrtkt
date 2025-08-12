package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.avdt.entities.UrlTable

fun Routing.urlShortenerRoutes() {

    post("/shorten") {
        val longUrl = URLBuilder(call.receiveText().trim()).buildString()
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
            println("redirecting $code to $it")
            return@get call.respondRedirect(it)
        }
        return@get call.respondText("URL not found", status = HttpStatusCode.NotFound)
    }

}