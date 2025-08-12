@file:OptIn(ExperimentalTime::class)

package xyz.avdt.models

import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Url(
    val id: String = UUID.randomUUID().toString(),
    val shortCode: String,
    val redirectUrl: String,
    val createdAt: Instant = Clock.System.now(),
    val expiresAt: Instant? = null,
)
