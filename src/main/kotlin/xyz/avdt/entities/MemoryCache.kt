package xyz.avdt.entities

import org.apache.commons.collections4.map.LRUMap
import xyz.avdt.models.ShortUrlCacheResponse

class MemoryCache(
    limit: Int,
) {
    private val cache: LRUMap<String, ShortUrlCacheResponse> = LRUMap(limit)

    fun add(key: String, value: ShortUrlCacheResponse) {
        cache[key] = value
    }


    fun remove(key: String) {
        if (cache.contains(key)) {
            cache.remove(key)
        }
    }

    fun fetch(key: String): ShortUrlCacheResponse? {
        return if (cache.contains(key) && cache[key] != null) {
            cache[key]!!
        } else {
            null
        }
    }

}