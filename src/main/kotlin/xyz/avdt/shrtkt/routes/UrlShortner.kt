package xyz.avdt.shrtkt.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.avdt.shrtkt.service.UrlShortnerService
import java.net.URI


fun Routing.urlShortnerRoutes(service: UrlShortnerService) {
    post("/shorten") {
        val urlToShorten = call.queryParameters["url"] ?: return@post call.respond(
            message = mapOf("error" to "No URL provided"), status = HttpStatusCode.BadRequest
        )
        val shortUrl = service.shortenUrl(URI(urlToShorten).toString())
        call.respond(message = shortUrl, status = HttpStatusCode.Created)
    }

    get("/expand/{shortcode}") {
        val shortcode = call.pathParameters["shortcode"] ?: return@get call.respond(
            message = mapOf("error" to "Invalid short-url provided"), status = HttpStatusCode.BadRequest
        )
        val shortUrl = service.expandUrl(shortcode)
        if (shortUrl == null) {
            call.respond(
                message = mapOf("error" to "No URL found for shortcode $shortcode"), status = HttpStatusCode.NotFound
            )
        } else {
            call.respond(
                message = shortUrl, status = HttpStatusCode.OK
            )
        }
    }
}