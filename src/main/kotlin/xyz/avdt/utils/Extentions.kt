package xyz.avdt.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.utils.io.*
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toStdlibInstant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) =
    now().toStdlibInstant().toLocalDateTime(timeZone = tz)

fun Application.getDatabaseEnv(key: String): String? = environment.config.tryGetString("ktor.deployment.database.$key")

sealed interface Resource<T> {
    data class Result<T>(val data: T) : Resource<T>
    data class Error<T>(val code: HttpStatusCode, val message: String = code.description) : Resource<T>
}

inline fun <R> runCatchingSafe(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        }
        Result.failure(e)
    }
}