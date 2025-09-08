package xyz.avdt.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.avdt.entities.UserTable

fun Routing.commonRoutes() {
    get("/") {
        call.respondText("Shrtkt: Url Shortener is running!")
    }
    get("/status") {
        val count = transaction {
            runCatching {
                UserTable.selectAll().count()
            }.getOrElse { -1L }
        }
        if (count >= 0L) {
            call.respondText("OK", status = HttpStatusCode.OK)
        } else {
            call.respondText("Server Error", status = HttpStatusCode.InternalServerError)
        }
    }
    get("/top-urls") {
        call.respondRedirect("/stats/top-urls")
    }
}