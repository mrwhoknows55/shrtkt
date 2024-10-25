package xyz.avdt.shrtkt.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureMonitoring() {
//    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

//    install(MicrometerMetrics) {
//        registry = appMicrometerRegistry
//        // ... TODO
//    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
