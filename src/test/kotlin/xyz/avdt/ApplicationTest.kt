package xyz.avdt

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testRoot() = testApplication {
        setup()
        val rootRes = client.get("/")
        assertEquals(HttpStatusCode.OK, rootRes.status)
        assertEquals("Shrtkt: Url Shortener is running!", rootRes.bodyAsText())

        val statusRes = client.get("/status")
        assertEquals(HttpStatusCode.OK, statusRes.status)
        assertEquals("OK", statusRes.bodyAsText())

    }

    @Test
    fun testShortenAndRedirect() = testApplication {
        setup()
        val targetLoc = "https://avdt.xyz"

        val shortRes = client.post("/shorten") {
            setBody(targetLoc)
        }

        assertEquals(HttpStatusCode.Created, shortRes.status)
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        assert(shortCode != null)

        val redirectRes = client.get("/redirect?code=$shortCode")
        assertEquals(HttpStatusCode.Found, redirectRes.status)
        val resultLocation = redirectRes.headers["Location"]
        assertEquals(targetLoc, resultLocation)

    }

    fun ApplicationTestBuilder.setup() {
        application {
            module(true)
        }
        client = createClient {
            followRedirects = false
        }
    }
}