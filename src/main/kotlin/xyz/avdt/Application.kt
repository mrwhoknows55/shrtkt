package xyz.avdt

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.sentry.Sentry
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.read
import xyz.avdt.plugins.*

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
        configureMonitoring()
    }

    val blacklist = DataFrame.read("blacklist.json")
    configureLatencyMeasurement()
    configureDatabases()
    configureCallLogging()
    configureSerialization()
    configureAPIKeyBlacklist(blacklist)
    configureAuth()
    // setup routes at last
    configureRouting()

    if (isTesting) {
        println("Application started in testing mode")
    }
}
