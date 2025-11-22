package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import xyz.avdt.entities.MemoryCache
import xyz.avdt.routes.commonRoutes
import xyz.avdt.routes.urlShortenerRoutes

fun Application.configureRouting(cache: MemoryCache) = routing {
    commonRoutes()
    urlShortenerRoutes(cache)
}

val nonAuthRoutes = arrayOf("/", "/status", "/top-urls", "/redirect", "/stats/top-urls", "/user")
