package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.configureLogging() {
    install(CallLogging) {
        format { call ->
            val ts = Clock.System.now()
            val status = call.response.status()
            val httpMethod = call.request.httpMethod
            val userAgent = call.request.userAgent()
            val ip = call.request.local.remoteAddress
            val local = call.request.local
            val path = call.request.path()
            "Time: $ts, HTTP method: $httpMethod, Path: $path, Status: $status, IP Address: $ip, User agent: $userAgent, Local: $local"
        }
    }
}

