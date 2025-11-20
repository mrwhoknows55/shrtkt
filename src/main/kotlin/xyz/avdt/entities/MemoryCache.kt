package xyz.avdt.entities

import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import redis.clients.jedis.Jedis
import xyz.avdt.models.ShortUrlCacheResponse
import xyz.avdt.utils.runCatchingSafe
import java.net.URI

class MemoryCache(environment: ApplicationEnvironment) {
    val url = environment.config.tryGetString("ktor.deployment.redis.url").orEmpty().ifBlank { "redis://0.0.0.0:6379" }
    private val client = Jedis(URI.create(url))

    suspend fun add(key: String, value: ShortUrlCacheResponse) = withContext(Dispatchers.IO) {
        runCatchingSafe {
            client.set(key, Json.encodeToString(value))
        }.onFailure {
            it.printStackTrace()
        }
        println("added key = $key to redis")
    }

    suspend fun fetch(key: String): ShortUrlCacheResponse? = withContext(Dispatchers.IO) {
        val valueStr = client.get(key).orEmpty()
        if (valueStr.isEmpty()) {
            return@withContext null
        }
        val value = runCatchingSafe {
            Json.decodeFromString<ShortUrlCacheResponse>(valueStr)
        }.onFailure { it.printStackTrace() }.getOrNull()
        return@withContext value
    }

    suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        runCatchingSafe {
            client.del(key)
        }.onFailure { it.printStackTrace() }
        println("removed $key from redis")
    }
}