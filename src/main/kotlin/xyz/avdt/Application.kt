package xyz.avdt

import io.ktor.server.application.*
import io.ktor.server.cio.*
import xyz.avdt.plugins.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(isTesting: Boolean = false) {
    configureLatencyMeasurement()
    configureDatabases()
    configureCallLogging()
    configureSerialization()
    configureAPIKeyBlacklist()
    configureAuth()
    // setup routes at last
    configureRouting()

    if (isTesting) {
        println("Application started in testing mode")
    }
}
