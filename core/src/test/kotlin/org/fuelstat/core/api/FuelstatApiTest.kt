package org.fuelstat.core.api

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FuelstatApiTest {
    private val server = MockWebServer()
    private var unauthorizedCalls = 0
    private lateinit var api: FuelstatApi

    @BeforeTest
    fun setUp() {
        server.start()
        api = FuelstatApi(
            baseUrl = server.url("/api/v1").toString(),
            client = OkHttpClient(),
            token = { "secret-token" },
            onUnauthorized = { unauthorizedCalls++ },
        )
    }

    @AfterTest
    fun tearDown() = server.close()

    private fun respond(code: Int, body: String) =
        server.enqueue(MockResponse.Builder().code(code).body(body).build())

    @Test
    fun `login without 2FA returns token and user`() = runTest {
        respond(200, """{"token":"jwt","user":{"id":"u1","email":"a@b.de"}}""")

        val result = api.login("a@b.de", "pw")

        assertEquals(LoginResult.Success("jwt", User(id = "u1", email = "a@b.de")), result)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/v1/auth/login", request.url.encodedPath)
        assertNull(request.headers["Authorization"], "sign-in must not send an old token")
        assertTrue(request.body!!.utf8().contains("\"email\":\"a@b.de\""))
    }

    @Test
    fun `login with 2FA asks for the code`() = runTest {
        respond(401, """{"error":"mfa_required","mfaToken":"mfa-1"}""")
        assertEquals(LoginResult.MfaRequired("mfa-1"), api.login("a@b.de", "pw"))

        respond(200, """{"token":"jwt","user":{"id":"u1","email":"a@b.de"}}""")
        assertEquals("jwt", api.loginMfa("mfa-1", "123456").token)
        server.takeRequest()
        assertTrue(server.takeRequest().body!!.utf8().contains("\"mfaToken\":\"mfa-1\""))
        assertEquals(0, unauthorizedCalls)
    }

    @Test
    fun `wrong password is an HTTP error with the API code`() = runTest {
        respond(401, """{"error":"invalid_credentials"}""")
        val error = assertFailsWith<ApiException.Http> { api.login("a@b.de", "wrong") }
        assertEquals("invalid_credentials", error.code)
        assertEquals(0, unauthorizedCalls)
    }

    @Test
    fun `authenticated calls send the token and report an ended session`() = runTest {
        respond(200, """[{"id":"v1","name":"Golf","fuel_type":"diesel"}]""")
        assertEquals(listOf("Golf"), api.vehicles().map { it.name })
        assertEquals("Bearer secret-token", server.takeRequest().headers["Authorization"])

        respond(401, """{"error":"token_revoked"}""")
        assertFailsWith<ApiException.Http> { api.vehicles() }
        assertEquals(1, unauthorizedCalls)
    }

    @Test
    fun `non-JSON answer is an invalid response`() = runTest {
        respond(200, "<html>not fuelstat</html>")
        assertIs<ApiException.InvalidResponse>(runCatching { api.health() }.exceptionOrNull())
    }

    @Test
    fun `error without JSON body keeps the status`() = runTest {
        respond(502, "Bad Gateway")
        val error = assertFailsWith<ApiException.Http> { api.whoami() }
        assertEquals(502, error.status)
        assertEquals("http_502", error.code)
    }
}
