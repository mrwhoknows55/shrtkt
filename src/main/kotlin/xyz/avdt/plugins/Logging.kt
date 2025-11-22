package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import xyz.avdt.entities.ApiLogTable
import xyz.avdt.utils.currentLocalDateTime
import xyz.avdt.utils.getIpAddress
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
            val ip = call.getIpAddress()
            val path = call.request.path()
            val logLine =
                "Time: $ts, HTTP method: $httpMethod, Path: $path, Status: $status, IP Address: $ip, User agent: $userAgent"
            if (path.contains("opentelemetry")) {
                return@format logLine
            }
            CoroutineScope(Dispatchers.IO).launch {
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
            }
            logLine
        }
    }
}

