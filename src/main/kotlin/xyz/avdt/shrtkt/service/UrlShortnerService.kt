package xyz.avdt.shrtkt.service

import xyz.avdt.shrtkt.model.ShortUrl

interface UrlShortnerService {
    fun shortenUrl(url: String): ShortUrl
    fun expandUrl(shortCode: String): ShortUrl?
}