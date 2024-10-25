package xyz.avdt.shrtkt.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.avdt.shrtkt.routes.urlShortnerRoutes
import xyz.avdt.shrtkt.service.InMemoryUrlShortner

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("hello" to "world"))
        }
        urlShortnerRoutes(InMemoryUrlShortner())
    }
}
