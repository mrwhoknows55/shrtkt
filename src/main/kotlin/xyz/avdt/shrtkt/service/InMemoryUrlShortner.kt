package xyz.avdt.shrtkt.service

import xyz.avdt.shrtkt.model.ShortUrl
import java.util.concurrent.ConcurrentHashMap

class InMemoryUrlShortner(
    private val basUrl: String = "http://localhost:8080",
) : UrlShortnerService {
    private val data by lazy { ConcurrentHashMap<Long, String>() }

    @Volatile
    private var counter = 0L

    override fun shortenUrl(url: String): ShortUrl = ShortUrl(
        shortCode = (++counter).toString(), baseUrl = basUrl, targetUrl = url
    ).also {
        data[counter] = url
        println("counter: $counter")
    }

    override fun expandUrl(shortCode: String): ShortUrl? = data[shortCode.toLong()]?.let {
        println("expandedUrl found: $it")
        ShortUrl(
            shortCode = shortCode, baseUrl = basUrl, targetUrl = it
        )
    }

}