package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.values
import xyz.avdt.utils.timeIt

fun Application.configureAPIKeyBlacklist(blacklist: AnyFrame) {
    val apiKeyBlacklistPlugin = createApplicationPlugin(name = "APIKeyBlacklistPlugin") {
        onCall { call ->
            val time = timeIt {
                val apiKey = call.request.headers["x-api-key"].orEmpty()
                if (apiKey.isEmpty()) {
                    return@timeIt
                }
                val isBlacklisted = blacklist.values().any { it?.toString() == apiKey }
                if (isBlacklisted) {
                    call.respond(
                        HttpStatusCode.TooManyRequests, "Too Many Requests"
                    )
                }
            }
            println("blacklist middleware took $time")
        }
    }
    install(apiKeyBlacklistPlugin)
}