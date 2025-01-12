package xyz.avdt.plugins

import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json, json = Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
    }
}
