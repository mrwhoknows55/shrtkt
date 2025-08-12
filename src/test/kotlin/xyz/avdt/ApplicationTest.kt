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

    @Test
    fun testSameUrlShouldReturnSameShortCode() = testApplication {
        setup()
        val targetLoc = "https://avdt.xyz"

        val shortRes = client.post("/shorten") {
            setBody(targetLoc)
        }

        assertEquals(HttpStatusCode.Created, shortRes.status)
        val originalShortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        assert(originalShortCode != null)

        val duplicateRes = client.post("/shorten") {
            setBody(targetLoc)
        }
        assertEquals(HttpStatusCode.Created, shortRes.status)
        val duplicateShortCode = json.decodeFromString<JsonObject>(duplicateRes.bodyAsText())["shortCode"]
        assert(originalShortCode == duplicateShortCode)

    }

    @Test
    fun testNotFoundShortCode() = testApplication {
        setup()
        val redirectRes = client.get("/redirect?code=-1")
        assertEquals(HttpStatusCode.NotFound, redirectRes.status)
    }

    @Test
    fun testDeleteShortCode() = testApplication {
        setup()
        val shortRes = client.post("/shorten") {
            setBody("https://avdt.xyz")
        }
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]

        val deleteRes = client.delete("/shorten/$shortCode")
        assertEquals(HttpStatusCode.NoContent, deleteRes.status)

        val deleteRes2 = client.delete("/shorten/$shortCode")
        assertEquals(HttpStatusCode.NotFound, deleteRes2.status)
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