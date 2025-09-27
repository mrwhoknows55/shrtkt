package xyz.avdt

import io.ktor.server.application.*
import io.ktor.server.cio.*
import xyz.avdt.plugins.configureDatabases
import xyz.avdt.plugins.configureLogging
import xyz.avdt.plugins.configureRouting
import xyz.avdt.plugins.configureSerialization

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module(isTesting: Boolean = false) {
    configureLogging()
    configureDatabases()
    configureSerialization()
    // setup routes at last
    configureRouting()

    if (isTesting) {
        println("Application started in testing mode")
    }
}
