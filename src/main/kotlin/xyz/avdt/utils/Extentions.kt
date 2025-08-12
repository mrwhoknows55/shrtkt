package xyz.avdt.utils

import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun currentLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) = now().toLocalDateTime(timeZone = tz)

fun Application.getDatabaseEnv(key: String): String? = environment.config.tryGetString("ktor.deployment.database.$key")