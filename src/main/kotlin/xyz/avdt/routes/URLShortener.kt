package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

var count = 0
val urlMap = mutableMapOf<String, String>()
fun Routing.urlShortenerRoutes() {

    post("/shorten") {
        val longUrl = URLBuilder(call.receiveText()).buildString()
        count += 1
        call.respond(HttpStatusCode.Created, mapOf("shortCode" to count).also {
            urlMap["$count"] = longUrl
        })
    }

    get("/redirect") {
        val code = call.request.queryParameters["code"]
        urlMap[code]?.let {
            call.respondRedirect(it)
        } ?: call.respondText("URL not found", status = HttpStatusCode.NotFound)
    }

}