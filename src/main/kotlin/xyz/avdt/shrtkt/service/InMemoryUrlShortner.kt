package xyz.avdt.shrtkt.service

import xyz.avdt.shrtkt.model.ShortUrl

class InMemoryUrlShortner(
    private val basUrl: String = "http://localhost:8080",
) : UrlShortnerService {
    private val data by lazy { mutableMapOf<Long, String>() }
    private var counter = 0L

    override fun shortenUrl(url: String): ShortUrl = ShortUrl(
        shortCode = (++counter).toString(),
        baseUrl = basUrl,
        targetUrl = url
    ).also {
        data[counter] = url
    }

    override fun expandUrl(shortCode: String): ShortUrl? = data[shortCode.toLong()]?.let {
        ShortUrl(
            shortCode = shortCode,
            baseUrl = basUrl,
            targetUrl = it
        )
    }

}