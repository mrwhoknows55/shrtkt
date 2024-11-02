package xyz.avdt.shrtkt.model

import kotlinx.serialization.Serializable

@Serializable
data class ShortUrl(
    val shortCode: String,
    val baseUrl: String,
    val targetUrl: String,
)
