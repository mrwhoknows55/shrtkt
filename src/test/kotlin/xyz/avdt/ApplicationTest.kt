package xyz.avdt

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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


    @Test
    fun testInvalidUrl() = testApplication {
        setup()
        val shortRes = client.post("/shorten") {
            setBody("avdt.xyz")
        }
        assertEquals(HttpStatusCode.BadRequest, shortRes.status)

        val emptyRes = client.post("/shorten") {
            setBody("")
        }
        assertEquals(HttpStatusCode.BadRequest, emptyRes.status)

        val httpRes = client.post("/shorten") {
            setBody("http://avdt.xyz")
        }
        assertNotEquals(HttpStatusCode.BadRequest, httpRes.status)

        val httpsRes = client.post("/shorten") {
            setBody("https://abc.xyz/test url")
        }
        assertNotEquals(HttpStatusCode.BadRequest, httpsRes.status)
    }

    @Test
    fun testInvalidShortCode() = testApplication {
        setup()
        val redirectRes = client.get("/redirect")
        assertEquals(HttpStatusCode.BadRequest, redirectRes.status)

        val redirectRes2 = client.get("/redirect?code=random")
        assertEquals(HttpStatusCode.BadRequest, redirectRes2.status)
    }

    @Test
    fun testDifferentUrlsShouldGenerateDifferentCodes() = testApplication {
        setup()
        val shortRes1 = client.post("/shorten") {
            setBody("https://avdt.xyz")
        }
        val shortCode1 = json.decodeFromString<JsonObject>(shortRes1.bodyAsText())["shortCode"]

        val shortRes2 = client.post("/shorten") {
            setBody("https://mrwhoknows.com")
        }
        val shortCode2 = json.decodeFromString<JsonObject>(shortRes2.bodyAsText())["shortCode"]

        assertNotEquals(shortCode1, shortCode2)
    }

    @Test
    fun testInvalidDeleteRequest() = testApplication {
        setup()
        val deleteRes = client.delete("/shorten")
        assertEquals(HttpStatusCode.BadRequest, deleteRes.status)

        val deleteRes2 = client.delete("/shorten/x")
        assertEquals(HttpStatusCode.BadRequest, deleteRes2.status)
    }

    @Test
    fun testPutRequest() = testApplication {
        setup()
        val targetLoc1 = "https://avdt.xyz"
        val shortRes = client.post("/shorten") {
            setBody(targetLoc1)
        }
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]

        val redirectRes = client.get("/redirect?code=$shortCode")
        val resultLocation = redirectRes.headers["Location"]
        assertEquals(targetLoc1, resultLocation)

        val targetLoc2 = "https://mrwhoknows.com"
        val result = client.put("/shorten/$shortCode") {
            setBody(targetLoc2)
        }
        assertEquals(HttpStatusCode.BadRequest, result.status)

        val targetLoc3 = "https://random.com/${Random.nextInt(1, 100)}"
        client.put("/shorten/$shortCode") {
            setBody(targetLoc3)
        }
        val redirectAfterPutRes = client.get("/redirect?code=$shortCode")
        val resultLocation2 = redirectAfterPutRes.headers["Location"]
        assertEquals(targetLoc3, resultLocation2)
        client.put("/shorten/$shortCode") {
            setBody(targetLoc1)
        }
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