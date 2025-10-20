package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.values
import org.jetbrains.kotlinx.dataframe.io.read

const val BLACKLIST_FILE_PATH = "blacklist.json"
fun Application.configureAPIKeyBlacklist() {
    val apiKeyBlacklistPlugin = createApplicationPlugin(name = "APIKeyBlacklistPlugin") {
        onCall { call ->
            val apiKey = call.request.headers["x-api-key"].orEmpty()
            val blacklist = DataFrame.read(BLACKLIST_FILE_PATH)
            val isBlacklisted = blacklist.values().any { it?.toString() == apiKey }
            if (isBlacklisted) {
                call.respond(
                    HttpStatusCode.TooManyRequests, "Too Many Requests"
                )
            }
        }
    }
    install(apiKeyBlacklistPlugin)
}