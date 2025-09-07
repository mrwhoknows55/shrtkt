package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.plus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import xyz.avdt.entities.UrlTable
import xyz.avdt.entities.UrlTable.deletedAt
import xyz.avdt.entities.UrlTable.expiredAt
import xyz.avdt.entities.UrlTable.redirectUrl
import xyz.avdt.entities.UrlTable.shortCode
import xyz.avdt.entities.UserTable
import xyz.avdt.utils.Resource.Error
import xyz.avdt.utils.Resource.Result
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
            val params = call.receiveParameters()
            val encodedUrl = params["url"].orEmpty().trim()
            val longUrl = parseUrl(encodedUrl)?.toString() ?: return@post call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )
            val expiredAtStr = params["expiredAt"].orEmpty()
            val customShortCode = params["shortCode"].orEmpty().trim()
            val expiredAtTime = runCatching {
                if (expiredAtStr.isNotBlank()) {
                    LocalDateTime.parse(expiredAtStr)
                } else {
                    null
                }
            }.onFailure {
                return@post call.respondText(
                    "wrong request format for expiredAt param", status = HttpStatusCode.BadRequest
                )
            }.getOrNull()
            val result = transaction {
                runCatching {
                    val result = UrlTable.insert {
                        if (customShortCode.isNotBlank()) {
                            it[shortCode] = customShortCode
                        }
                        it[redirectUrl] = longUrl
                        it[UrlTable.userId] = userId
                        it[deletedAt] = null
                        it[expiredAt] = expiredAtTime
                    }
                    val res = (result get shortCode).orEmpty().ifBlank { (result get UrlTable.id).toString() }
                    Result(data = res)
                }.getOrElse {
                    when {
                        it.message.orEmpty()
                            .contains("violates unique constraint", true) -> Error(HttpStatusCode.Conflict)

                        else -> Error(HttpStatusCode.NotFound)
                    }
                }
            }

            when (result) {
                is Result -> {
                    println("added $longUrl on ${result.data} code")
                    call.respond(HttpStatusCode.Created, mapOf("shortCode" to result.data))
                }

                is Error -> {
                    when (result.code) {
                        HttpStatusCode.Conflict -> call.respondText(
                            "Short code already exists", status = HttpStatusCode.Conflict
                        )

                        HttpStatusCode.NotFound -> call.respondText("URL not found", status = HttpStatusCode.NotFound)
                        else -> call.respondText(result.code.description, status = result.code)
                    }
                }
            }
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
            val params = call.receiveParameters()
            val shortCode = call.request.pathVariables["shortCode"].orEmpty().ifBlank {
                return@put call.respondText(
                    "wrong request format for short code", status = HttpStatusCode.BadRequest
                )
            }
            val encodedUrl = params["url"].orEmpty().trim()
            val expiredAtStr = params["expiredAt"].orEmpty()
            val customCode = params["shortCode"].orEmpty().trim()
            val expiredAtTime = runCatching {
                if (expiredAtStr.isNotBlank()) {
                    LocalDateTime.parse(expiredAtStr)
                } else {
                    null
                }
            }.onFailure {
                return@put call.respondText(
                    "wrong request format for expiredAt param", status = HttpStatusCode.BadRequest
                )
            }.getOrNull()

            val urlToUpdate = parseUrl(encodedUrl)?.toString() ?: return@put call.respond(
                status = HttpStatusCode.BadRequest, "invalid url"
            )
            val count = transaction {
                UrlTable.update({
                    UrlTable.shortCodeEq(shortCode).and {
                        UrlTable.userId eq userId
                    }
                }) {
                    it[redirectUrl] = urlToUpdate
                    it[expiredAt] = expiredAtTime
                    if (customCode.isNotBlank()) {
                        it[UrlTable.shortCode] = customCode
                    }
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
            val shortCode = call.request.pathVariables["shortCode"]?.replace("\"", "").orEmpty().ifBlank {
                return@delete call.respondText(
                    "wrong request format for short code", status = HttpStatusCode.BadRequest
                )
            }
            val (id, error) = call.getUserId()
            if (error != null) {
                return@delete error
            }
            val userId = id!!

            val result = transaction {
                runCatching {
                    UrlTable.update({
                        UrlTable.shortCodeEq(shortCode).and { UrlTable.userId eq userId and deletedAt.isNull() }
                    }) {
                        it[deletedAt] = currentLocalDateTime()
                    }
                }.getOrElse { -1 }
            }

            if (result > 0) {
                println("shortCode: $shortCode deleted successfully")
                call.respondText("short code deleted successfully", status = HttpStatusCode.OK)
            } else {
                println("shortCode: $shortCode not found")
                call.respondText("short code not found for the user", status = HttpStatusCode.NotFound)
            }

        }
    }

    get("/redirect") {
        val code = call.request.queryParameters["code"]?.replace("\"", "") ?: return@get call.respondText(
            "wrong request format for short code", status = HttpStatusCode.BadRequest
        )
        val result = transaction {
            val codeL = code.trim().toLongOrNull() ?: 0
            runCatching {
                val longUrl = UrlTable.select(redirectUrl).where {
                    (UrlTable.id eq codeL) or (shortCode eq code)
                }.andWhere {
                    deletedAt.isNull()
                }.andWhere {
                    expiredAt.isNull() or (expiredAt greater currentLocalDateTime())
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
                        UrlTable.shortCodeEq(code)
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