@file:OptIn(ExperimentalTime::class)

package xyz.avdt.models

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime


@Serializable
data class UrlRequest(
    val redirectUrl: String,
    val shortCode: String? = null,
    val expiresAt: String? = null,
)


@Serializable
data class UrlResponse(
    val success: Boolean,
    val message: String,
    val redirectUrl: String?,
    val shortCode: String?,
    val expiresAt: String? = null,
)

@Serializable
data class BulkUrlResponse(
    val summary: Summary,
    val results: List<UrlResponse>,
) {
    @Serializable
    data class Summary(
        val total: Int,
        val success: Int,
        val error: Int,
    )
}