package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.commonRoutes() {
    get("/") {
        call.respondText("Shrtkt: Url Shortener is running!")
    }
    get("/status") {
        call.respondText("OK", status = HttpStatusCode.OK)
    }
}