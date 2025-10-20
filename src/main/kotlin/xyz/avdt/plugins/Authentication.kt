package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.avdt.entities.UserTable
import xyz.avdt.entities.UserTier


fun Application.configureAuth() {
    val authCheckerPlugin = createApplicationPlugin(name = "AuthCheckerPlugin") {
        onCall { call ->
            call.request.origin.apply {
                println("OnCall: Request URL: $scheme://$localHost:$localPort$uri")
            }
            val route = call.request.path()
            if (nonAuthRoutes.contains(route)) {
                println("No Auth Needed for the route $route")
                return@onCall
            }

            val apiKey = call.request.headers["x-api-key"].orEmpty()
            if (apiKey.isEmpty()) {
                call.respond(
                    HttpStatusCode.Unauthorized, "Invalid/Missing api key"
                )
            }
            val tierToCheck: UserTier? = if (route.contains("bulk")) UserTier.ENTERPRISE else null
            val userId = transaction {
                runCatching {
                    UserTable.select(UserTable.id).where { UserTable.apiKey eq apiKey }
                        .andWhere { tierToCheck?.let { UserTable.tier eq tierToCheck } ?: Op.TRUE }.limit(1)
                        .single()[UserTable.id]
                }.onFailure {
                    it.printStackTrace()
                }.getOrNull()
            } ?: run {
                return@onCall call.respond(
                    HttpStatusCode.Unauthorized, "Invalid API key or access tier not permitted."
                )
            }
            println("Processing url $route for user_id $userId")
        }
    }
    install(authCheckerPlugin)
}