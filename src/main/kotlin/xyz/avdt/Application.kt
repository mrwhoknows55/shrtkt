package xyz.avdt

import io.ktor.server.application.*
import io.ktor.server.cio.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.read
import xyz.avdt.plugins.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(isTesting: Boolean = false) {
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
