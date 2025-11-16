package xyz.avdt.models

import kotlinx.serialization.Serializable

@Serializable
data class ShortUrlCacheResponse(
    val shortCode: String,
    val redirectUrl: String,
    val password: String?,
)
