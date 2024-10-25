package xyz.avdt.shrtkt.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.avdt.shrtkt.service.UrlShortner
import java.net.URI


fun Routing.urlShortnerRoutes(service: UrlShortner) {
    post("/shorten") {
        val urlToShorten = call.queryParameters["url"] ?: return@post call.respond(
            message = mapOf("error" to "No URL provided"), status = HttpStatusCode.BadRequest
        )
        val shortUrl = service.shortenUrl(URI(urlToShorten).toString())
        call.respond(message = mapOf("shortUrl" to shortUrl), status = HttpStatusCode.Created)
    }

    get("/expand/{shortcode}") {
        val shortcode = call.pathParameters["shortcode"] ?: return@get call.respond(
            message = mapOf("error" to "Invalid short url provided"), status = HttpStatusCode.BadRequest
        )
        val longUrl = service.expandUrl(shortcode)
        if (longUrl == null) {
            call.respond(
                message = mapOf("error" to "No URL found for shortcode $shortcode"), status = HttpStatusCode.NotFound
            )
        } else {
            call.respond(
                message = mapOf("longUrl" to longUrl), status = HttpStatusCode.OK
            )
        }
    }
}