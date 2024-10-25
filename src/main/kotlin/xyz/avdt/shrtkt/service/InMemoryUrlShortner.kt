package xyz.avdt.shrtkt.service

class InMemoryUrlShortner(
    private val basUrl: String = "http://localhost:8080",
) : UrlShortner {
    private val data by lazy { mutableMapOf<Long, String>() }
    private var counter = 0L

    override fun shortenUrl(url: String): String {
        data[++counter] = url
        println("shortening $url to $counter")
        return "$basUrl/${counter}"
    }

    override fun expandUrl(shortcode: String): String? {
        val id = shortcode.toLong()
        return data[id]
    }
}