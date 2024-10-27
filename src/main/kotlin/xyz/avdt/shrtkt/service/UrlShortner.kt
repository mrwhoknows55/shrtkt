package xyz.avdt.shrtkt.service

interface UrlShortner {
    fun shortenUrl(url: String): String
    fun expandUrl(shortcode: String): String?
}