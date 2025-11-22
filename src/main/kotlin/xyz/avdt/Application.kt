package xyz.avdt

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.config.*
import io.sentry.Sentry
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.read
import redis.clients.jedis.Jedis
import xyz.avdt.entities.MemoryCache
import xyz.avdt.plugins.*
import xyz.avdt.utils.runCatchingSafe
import java.io.File
import java.net.URI

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(isTesting: Boolean = false) {
    if (!isTesting) {
        Sentry.init { options ->
            options.dsn =
                "https://e1282d33716845dad0d7ed107af4e33c@o4510228991311872.ingest.us.sentry.io/4510228993540096"
            options.tracesSampleRate = 1.0
            options.isDebug = false
        }
//        configureMonitoring()
    }

    val url = environment.config.tryGetString("ktor.deployment.redis.url").orEmpty().ifBlank { "redis://0.0.0.0:6379" }
    val jedis = Jedis(URI.create(url))
    val cache = MemoryCache(jedis)
    if (isTesting) {
        jedis.use {
            it.del("rl:localhost")
        }
    }

    val blacklist = runCatchingSafe {
        val root = System.getProperty("user.dir")
        val path = File(root, "blacklist.json")
        DataFrame.read(path)
    }.onFailure { println(it) }.getOrNull()
    configureLatencyMeasurement()
    configureDatabases()
    configureCallLogging()
    configureSerialization()
    configureRateLimiter(jedis)
    configureAPIKeyBlacklist(blacklist)
    configureAuth()
    // setup routes at last
    configureRouting(cache)

    if (isTesting) {
        println("Application started in testing mode")
    }
}