package xyz.avdt.entities

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import redis.clients.jedis.Jedis
import xyz.avdt.models.ShortUrlCacheResponse
import xyz.avdt.utils.runCatchingSafe
import java.net.URI

class MemoryCache(url: String) {
    private val client = Jedis(URI.create(url))

    suspend fun add(key: String, value: ShortUrlCacheResponse) = withContext(Dispatchers.IO) {
        client.set(key, Json.encodeToString(value))
        println("added key = $key to redis")
    }


    suspend fun fetch(key: String): ShortUrlCacheResponse? = withContext(Dispatchers.IO) {
        val value = runCatchingSafe {
            Json.decodeFromString<ShortUrlCacheResponse>(client.get(key)!!)
        }.onFailure { it.printStackTrace() }.getOrNull()
        return@withContext value
    }
}