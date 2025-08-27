package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import xyz.avdt.entities.UrlTable
import xyz.avdt.entities.UrlTable.deletedAt
import xyz.avdt.entities.UrlTable.redirectUrl
import xyz.avdt.entities.UrlTable.shortCode
import xyz.avdt.entities.UserTable
import xyz.avdt.utils.currentLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Routing.urlShortenerRoutes() {

    suspend fun RoutingCall.getUserId(): Pair<Long?, Unit?> {
        val apiKey = request.headers["x-api-key"].orEmpty()
        if (apiKey.isEmpty()) {
            return null to respond(
                HttpStatusCode.Unauthorized, "Invalid/Missing api key"
            )
        }
        val userId = transaction {
            runCatching {
                UserTable.select(UserTable.id).where { UserTable.apiKey eq apiKey }.limit(1).single()[UserTable.id]
            }.onFailure {
                it.printStackTrace()

            }.getOrNull()
        } ?: return (null to respond(
            HttpStatusCode.Unauthorized, "Invalid api key"
        ))
        return userId to null
    }

    route("/shorten") {
        post {
            val (id, error) = call.getUserId()
            if (error != null) {
                return@post error
            }
            val userId = id!!
            val encodedUrl = runCatching { call.receiveText().trim() }.getOrElse { "" }
            val longUrl = parseUrl(encodedUrl)?.toString() ?: return@post call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )
            val shortCode = transaction {
                runCatching {
                    UrlTable.insert {
                        it[redirectUrl] = longUrl
                        it[UrlTable.userId] = userId
                        it[deletedAt] = null
                    } get shortCode
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
            val (id, error) = call.getUserId()
            if (error != null) {
                return@put error
            }
            val userId = id!!
            val shortCode = call.request.pathVariables["shortCode"]?.toLongOrNull() ?: return@put call.respondText(
                "wrong request format for short code", status = HttpStatusCode.BadRequest
            )
            val encodedUrl = runCatching { call.receiveText().trim() }.getOrElse { "" }
            val urlToUpdate = parseUrl(encodedUrl)?.toString() ?: return@put call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )
            val count = transaction {
                UrlTable.update({
                    UrlTable.shortCode eq shortCode
                    UrlTable.userId eq userId
                }) {
                    it[redirectUrl] = urlToUpdate
                    it[deletedAt] = null
                }
            }

            if (count > 0) {
                println("updated url $urlToUpdate on shortCode: $shortCode")
                return@put call.respond(HttpStatusCode.Accepted, mapOf("shortCode" to shortCode))
            } else {
                return@put call.respondText(
                    "URL for the short code doesn't exist for this user", status = HttpStatusCode.NotFound
                )
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
            val (id, error) = call.getUserId()
            if (error != null) {
                return@delete error
            }
            val userId = id!!

            val result = transaction {
                runCatching {
                    UrlTable.update({
                        UrlTable.shortCode eq shortCode
                        UrlTable.userId eq userId
                        deletedAt.isNull()
                    }) {
                        it[deletedAt] = currentLocalDateTime()
                    }
                }.getOrElse { -1 }
            }

            if (result > 0) {
                println("shortCode: $shortCode deleted successfully")
                call.respondText("short code deleted successfully", status = HttpStatusCode.NoContent)
            } else {
                println("shortCode: $shortCode not found")
                call.respondText("short code not found for the user", status = HttpStatusCode.NotFound)
            }

        }
    }

    get("/redirect") {
        val code = call.request.queryParameters["code"]?.toLongOrNull() ?: return@get call.respondText(
            "wrong request format for short code", status = HttpStatusCode.BadRequest
        )
        val result = transaction {
            runCatching {
                val longUrl = UrlTable.select(redirectUrl).where {
                    shortCode eq code
                }.andWhere {
                    deletedAt.isNull()
                }.single()[redirectUrl]
                return@transaction longUrl
            }.onFailure {
                println("${it.message}")
            }.getOrNull()
        }
        result?.let { url ->
            runCatching {
                transaction {
                    UrlTable.update(where = {
                        shortCode eq code
                        deletedAt.isNull()
                    }) {
                        it[visitCount] = visitCount + 1
                        it[lastAccessedAt] = currentLocalDateTime()
                    }
                }
            }
            println("redirecting $code to its target destination")
            return@get call.respondRedirect(url)
        }
        return@get call.respondText("URL not found", status = HttpStatusCode.NotFound)
    }

    get("/stats/top-urls") {
        println("Fetching top 10 URLs by shortening frequency")

        val topUrls = transaction {
            val urlCounts =
                UrlTable.select(redirectUrl, redirectUrl.count()).where { deletedAt eq null }.groupBy(redirectUrl)
                    .limit(10).map { row ->
                        val url = row[redirectUrl]
                        val count = row[redirectUrl.count()]
                        url to count
                    }

            urlCounts.sortedByDescending { it.second }.map { (url, count) ->
                mapOf(
                    "url" to url, "shortenCount" to count.toString()
                )
            }
        }

        println("Found ${topUrls.size} top URLs")
        call.respond(HttpStatusCode.OK, mapOf("topUrls" to topUrls))
    }

    post("/user") {
        val apiKey = call.request.headers["x-api-key"].orEmpty().ifBlank {
            return@post call.respond(
                HttpStatusCode.Unauthorized, "Invalid/Missing api key"
            )
        }
        val params = call.receiveParameters()
        val name = params["name"].orEmpty()
        val email = params["email"].orEmpty()
        if (name.isEmpty() || email.isEmpty()) {
            return@post call.respond(
                HttpStatusCode.BadRequest, "Missing parameters"
            )
        }
        transaction {
            runCatching {
                UserTable.insert {
                    it[UserTable.name] = name
                    it[UserTable.apiKey] = apiKey
                    it[UserTable.email] = email
                }
            }
        }.onSuccess {
            return@post call.respond(
                HttpStatusCode.Created, "User created"
            )
        }.onFailure {
            if (it.cause.toString().contains("duplicate")) {
                return@post call.respond(
                    HttpStatusCode.Created, "User created"
                )
            }
            it.printStackTrace()
            return@post call.respond(
                HttpStatusCode.InternalServerError, "Error while creating user"
            )
        }
    }

}