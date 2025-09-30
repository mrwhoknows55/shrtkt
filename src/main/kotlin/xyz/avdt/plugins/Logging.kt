package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.avdt.entities.ApiLogTable
import xyz.avdt.utils.currentLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.configureCallLogging() {
    install(CallLogging) {
        format { call ->
            val ts = Clock.System.now()
            val status = call.response.status()
            val httpMethod = call.request.httpMethod
            val userAgent = call.request.userAgent()
            val ip = call.request.local.remoteAddress
            val local = call.request.local
            val path = call.request.path()
            val logLine =
                "Time: $ts, HTTP method: $httpMethod, Path: $path, Status: $status, IP Address: $ip, User agent: $userAgent, Local: $local"
            val startTime = Clock.System.now()
            transaction {
                ApiLogTable.insert {
                    it[ApiLogTable.createdAt] = currentLocalDateTime()
                    it[ApiLogTable.httpMethod] = httpMethod.toString()
                    it[ApiLogTable.path] = path
                    it[ApiLogTable.status] = status?.value ?: -1
                    it[ApiLogTable.ipAddress] = ip
                    it[ApiLogTable.userAgent] = userAgent.toString()
                }
            }
            val diff = Clock.System.now() - startTime
            println("log insert latency: $diff")
            logLine
        }
    }
}

