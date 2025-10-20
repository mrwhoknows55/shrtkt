package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import xyz.avdt.routes.commonRoutes
import xyz.avdt.routes.urlShortenerRoutes

fun Application.configureRouting() = routing {
    commonRoutes()
    urlShortenerRoutes()
}

val nonAuthRoutes = arrayOf("/", "/status", "/top-urls", "/redirect", "/stats/top-urls")
