package xyz.avdt.shrtkt

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import xyz.avdt.shrtkt.plugins.configureHTTP
import xyz.avdt.shrtkt.plugins.configureMonitoring
import xyz.avdt.shrtkt.plugins.configureRouting
import xyz.avdt.shrtkt.plugins.configureSerialization

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
