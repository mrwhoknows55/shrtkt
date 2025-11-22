package xyz.avdt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import redis.clients.jedis.Jedis
import xyz.avdt.utils.getIpAddress


fun Application.configureRateLimiter(jedis: Jedis) {
    val rateLimiter = createApplicationPlugin(name = "RateLimiter") {
        onCall { call ->
            val ip = call.getIpAddress()
            val key = "rl:$ip"
            jedis.use { jedis ->
                if (jedis.setnx(key, "1") == 1L) {
                    jedis.expire(key, 60)
                } else {
                    val count = jedis.get(key).toLongOrNull() ?: 0
                    if (count > 100) {
                        val ttl = jedis.ttl(key)
                        println("IP `$ip` rate limited for $ttl seconds")
                        call.respond(
                            HttpStatusCode.TooManyRequests, "Too Many Requests"
                        )
                    }
                    jedis.incr(key)
                }
            }
        }
    }
    install(rateLimiter)
}
