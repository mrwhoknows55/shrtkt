package xyz.avdt.plugins

import io.ktor.server.application.*
import io.ktor.utils.io.*
import xyz.avdt.utils.runCatchingSafe
import java.lang.System.currentTimeMillis
import kotlin.time.Duration.Companion.milliseconds

const val HEADER_REQUEST_START_TIME = "Request-Start-Time"
const val HEADER_SERVER_TIMING = "Server-Timing"

@OptIn(InternalAPI::class)
fun Application.configureLatencyMeasurement() {
    val latencyManagementPlugin = createApplicationPlugin(name = "LatencyMeasurementPlugin") {
        onCall { call ->
            val currentTime = currentTimeMillis().milliseconds.inWholeMilliseconds.toString()
            call.response.headers.append(HEADER_REQUEST_START_TIME, currentTime)
            println(call.response.headers)
        }
        onCallRespond { call ->
            runCatchingSafe {
                val totalLatency = call.response.headers[HEADER_REQUEST_START_TIME]?.toLongOrNull()?.let { startTime ->
                    currentTimeMillis() - startTime
                }?.milliseconds

                totalLatency?.let {
                    val header = HEADER_SERVER_TIMING to "total;dur=${it}"
                    call.response.headers.append(header.first, header.second)
                }
            }
        }
    }
    install(latencyManagementPlugin)
}