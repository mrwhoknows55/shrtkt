package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.avdt.entities.UserTable
import xyz.avdt.entities.UserTier
import xyz.avdt.utils.runCatchingSafe
import xyz.avdt.utils.timeIt


const val HEADER_USER_ID = "User-Id"

@OptIn(InternalAPI::class)
fun Application.configureAuth() {
    val authCheckerPlugin = createApplicationPlugin(name = "authCheckerPlugin") {
        onCall { call ->
            val time = timeIt {
                call.request.origin.apply {
                    println("Request URL: $uri")
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
                    runCatchingSafe {
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
                call.request.setHeader(HEADER_USER_ID, listOf(userId.toString()))
                println("Processing url $route for user_id $userId")
            }
            call.response.headers.append(
                HEADER_SERVER_TIMING, "auth;dur=${time.inWholeMilliseconds}"
            )
            println("authChecker middleware took $time")
        }
    }
    install(authCheckerPlugin)
}