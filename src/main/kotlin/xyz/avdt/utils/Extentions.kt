package xyz.avdt.utils

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toStdlibInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) =
    now().toStdlibInstant().toLocalDateTime(timeZone = tz)

fun Application.getDatabaseEnv(key: String): String? = environment.config.tryGetString("ktor.deployment.database.$key")