package xyz.avdt

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone.Companion.currentSystemDefault
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import xyz.avdt.utils.currentLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

class ApplicationTest {
    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
    val apiKey = "1234"
    val userName = "test"
    val email = "test"
    fun HttpRequestBuilder.addXApiKey(xApiKey: String = apiKey) {
        header("x-api-key", xApiKey)
    }

    fun HttpRequestBuilder.setBodyX(url: String, expiredAt: LocalDateTime? = null) {
        setBody(FormDataContent(Parameters.build {
            append("url", url)
            expiredAt?.let {
                append("expiredAt", it.toString())
            }
        }))
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
            setBodyX(targetLoc)
            addXApiKey()
        }

        assertEquals(HttpStatusCode.Created, shortRes.status)
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        assert(shortCode != null)

        val redirectRes = client.get("/redirect?code=$shortCode")
        assertEquals(HttpStatusCode.Found, redirectRes.status)
        val resultLocation = redirectRes.headers["Location"]
        assertEquals(targetLoc, resultLocation)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testShortenAndRedirectForExpiredAt() = testApplication {
        setup()
        val targetLoc = "https://avdt.xyz"

        val shortRes = client.post("/shorten") {
            setBodyX(targetLoc, currentLocalDateTime())
            addXApiKey()
        }

        assertEquals(HttpStatusCode.Created, shortRes.status)
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        assert(shortCode != null)

        val redirectRes = client.get("/redirect?code=$shortCode")
        assertEquals(HttpStatusCode.NotFound, redirectRes.status)


        val shortRes2 = client.post("/shorten") {
            setBody(FormDataContent(Parameters.build {
                append("url", targetLoc)
                val tomorrow = (Clock.System.now() + 24.hours).toLocalDateTime(currentSystemDefault())
                append("expiredAt", tomorrow.toString())
            }))
            addXApiKey()
        }
        assertEquals(HttpStatusCode.Created, shortRes2.status)
        val shortCode2 = json.decodeFromString<JsonObject>(shortRes2.bodyAsText())["shortCode"]
        assert(shortCode2 != null)
        val redirectRes2 = client.get("/redirect?code=$shortCode2")
        assertEquals(HttpStatusCode.Found, redirectRes2.status)
        val resultLocation = redirectRes2.headers["Location"]
        assertEquals(targetLoc, resultLocation)
    }

    @Test
    fun testSameUrlShouldReturnDifferentShortCodes() = testApplication {
        setup()
        val targetLoc = "https://avdt.xyz"

        val shortRes = client.post("/shorten") {
            setBodyX(targetLoc)
            addXApiKey()
        }

        assertEquals(HttpStatusCode.Created, shortRes.status)
        val originalShortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        assert(originalShortCode != null)

        val duplicateRes = client.post("/shorten") {
            setBodyX(targetLoc)
            addXApiKey()
        }
        assertEquals(HttpStatusCode.Created, duplicateRes.status)
        val duplicateShortCode = json.decodeFromString<JsonObject>(duplicateRes.bodyAsText())["shortCode"]
        assertNotEquals(originalShortCode, duplicateShortCode)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testTopUrlsEndpoint() = testApplication {
        setup()

        val targetLoc = "https://avdt.xyz"

        client.post("/shorten") {
            setBodyX(targetLoc)
            addXApiKey()
        }

        client.post("/shorten") {
            setBodyX(targetLoc)
            addXApiKey()
        }

        client.post("/shorten") {
            setBodyX(targetLoc)
            addXApiKey()
        }

        val differentUrl = "https://mrwhoknows.com"
        client.post("/shorten") {
            setBodyX(differentUrl)
            addXApiKey()
        }

        val topUrlsRes = client.get("/stats/top-urls")
        assertEquals(HttpStatusCode.OK, topUrlsRes.status)

        val responseBody = topUrlsRes.bodyAsText()
        assert(responseBody.contains(targetLoc))
        assert(responseBody.contains(differentUrl))
    }

    @Test
    fun testTopUrlsRedirect() = testApplication {
        setup()

        val topUrlsRes = client.get("/top-urls")
        assertEquals(HttpStatusCode.Found, topUrlsRes.status)

        val redirectRes = client.get("/stats/top-urls")
        assertEquals(HttpStatusCode.OK, redirectRes.status)
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
            setBody(FormDataContent(Parameters.build {
                append("url", "https://avdt.xyz")
            }))
            addXApiKey()
        }
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]
        val deleteRes = client.delete("/shorten/$shortCode") { addXApiKey() }
        assertEquals(HttpStatusCode.NoContent, deleteRes.status)

        val deleteRes2 = client.delete("/shorten/$shortCode") { addXApiKey() }
        assertEquals(HttpStatusCode.NotFound, deleteRes2.status)

        val deleteRes3 = client.delete("/shorten/$shortCode") { addXApiKey("") }
        assertEquals(HttpStatusCode.Unauthorized, deleteRes3.status)
    }

    @Test
    fun testInvalidUrl() = testApplication {
        setup()
        val shortRes = client.post("/shorten") {
            setBodyX("avdt.xyz")
            addXApiKey()
        }
        assertEquals(HttpStatusCode.BadRequest, shortRes.status)

        val emptyRes = client.post("/shorten") {
            setBodyX("")
            addXApiKey()
        }
        assertEquals(HttpStatusCode.BadRequest, emptyRes.status)

        val httpRes = client.post("/shorten") {
            setBodyX("http://avdt.xyz")
            addXApiKey()
        }
        assertNotEquals(HttpStatusCode.BadRequest, httpRes.status)

        val httpsRes = client.post("/shorten") {
            setBodyX("https://abc.xyz/test url")
            addXApiKey()
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
            setBodyX("https://avdt.xyz")
            addXApiKey()
        }
        val shortCode1 = json.decodeFromString<JsonObject>(shortRes1.bodyAsText())["shortCode"]

        val shortRes2 = client.post("/shorten") {
            setBodyX("https://mrwhoknows.com")
            addXApiKey()
        }
        val shortCode2 = json.decodeFromString<JsonObject>(shortRes2.bodyAsText())["shortCode"]

        assertNotEquals(shortCode1, shortCode2)
    }

    @Test
    fun testInvalidDeleteRequest() = testApplication {
        setup()
        val deleteRes = client.delete("/shorten") {
            addXApiKey()
        }
        assertEquals(HttpStatusCode.BadRequest, deleteRes.status)

        val deleteRes2 = client.delete("/shorten/x") {
            addXApiKey()
        }
        assertEquals(HttpStatusCode.BadRequest, deleteRes2.status)
    }

    @Test
    fun testPutRequest() = testApplication {
        setup()
        val targetLoc1 = "https://avdt.xyz"
        val shortRes = client.post("/shorten") {
            setBodyX(targetLoc1)
            addXApiKey()
        }
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]

        val redirectRes = client.get("/redirect?code=$shortCode")
        val resultLocation = redirectRes.headers["Location"]
        assertEquals(targetLoc1, resultLocation)

        val targetLoc2 = "https://mrwhoknows.com"
        val result = client.put("/shorten/$shortCode") {
            setBodyX(targetLoc2)
            addXApiKey()
        }
        assertEquals(HttpStatusCode.Accepted, result.status)

        val withoutApiKey = client.put("/shorten/$shortCode") { setBodyX(targetLoc2) }
        assertEquals(HttpStatusCode.Unauthorized, withoutApiKey.status)

        val redirectAfterPutRes = client.get("/redirect?code=$shortCode")
        val resultLocation2 = redirectAfterPutRes.headers["Location"]
        assertEquals(targetLoc2, resultLocation2)

        client.put("/shorten/$shortCode") {
            setBodyX(targetLoc1)
            addXApiKey()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testPutRequestForExpiredAt() = testApplication {
        val tomorrow = (Clock.System.now() + 24.hours).toLocalDateTime(currentSystemDefault())
        setup()
        val targetLoc1 = "https://avdt.xyz"
        val shortRes = client.post("/shorten") {
            setBodyX(targetLoc1)
            addXApiKey()
        }
        val shortCode = json.decodeFromString<JsonObject>(shortRes.bodyAsText())["shortCode"]

        val redirectRes = client.get("/redirect?code=$shortCode")
        val resultLocation = redirectRes.headers["Location"]
        assertEquals(targetLoc1, resultLocation)

        val targetLoc2 = "https://mrwhoknows.com"
        val result = client.put("/shorten/$shortCode") {
            setBodyX(targetLoc2, currentLocalDateTime())
            addXApiKey()
        }
        assertEquals(HttpStatusCode.Accepted, result.status)

        val redirectAfterPutRes = client.get("/redirect?code=$shortCode")
        assertEquals(HttpStatusCode.NotFound, redirectAfterPutRes.status)

        val result2 = client.put("/shorten/$shortCode") {
            setBodyX(targetLoc2, tomorrow)
            addXApiKey()
        }
        assertEquals(HttpStatusCode.Accepted, result2.status)

        val redirectAfterPutRes2 = client.get("/redirect?code=$shortCode")
        val resultLocation2 = redirectAfterPutRes2.headers["Location"]
        assertEquals(targetLoc2, resultLocation2)

    }

    fun ApplicationTestBuilder.setup() {
        application {
            module(true)
        }
        client = createClient {
            followRedirects = false
        }
    }


    init {
        testApplication {
            setup()
            client.post("/user") {
                setBody(FormDataContent(Parameters.build {
                    append("name", userName)
                    append("email", email)
                }))
                addXApiKey()
            }
        }
    }
}