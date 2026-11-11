package org.fuelstat.core.api

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Result of a sign-in attempt. */
public sealed interface LoginResult {
    public data class Success(val token: String, val user: User) : LoginResult

    /** 2FA is active: send the code with [FuelstatApi.loginMfa] and this token (valid 5 minutes). */
    public data class MfaRequired(val mfaToken: String) : LoginResult
}

/**
 * Client for the Fuelstat REST API (`/api/v1`, see `docs/api.md` in FuelStat/fuelstat).
 *
 * The API is the source of truth: this class only transports data, it does not calculate anything.
 *
 * @param baseUrl API base URL, e.g. `https://tanken.1emc.de/api/v1` (see [org.fuelstat.core.ServerAddress]).
 * @param token returns the current access token, or null when signed out.
 * @param onUnauthorized called when an authenticated request is rejected because the session ended.
 */
public class FuelstatApi(
    baseUrl: String,
    private val client: OkHttpClient,
    private val userAgent: String = "Fuelstat-Android",
    private val token: () -> String? = { null },
    private val onUnauthorized: () -> Unit = {},
) {
    private val base: HttpUrl = (baseUrl.trimEnd('/') + "/").toHttpUrl()

    public suspend fun health(): Health = get("health", Health.serializer(), authenticated = false)

    /** `POST /auth/login`. Returns [LoginResult.MfaRequired] when the account has 2FA. */
    public suspend fun login(email: String, password: String): LoginResult {
        val body = buildJsonObject {
            put("email", email)
            put("password", password)
        }
        return try {
            val response = send("auth/login", "POST", body.toBody(), AuthResponse.serializer(), authenticated = false)
            LoginResult.Success(response.token, response.user)
        } catch (e: ApiException.Http) {
            val mfaToken = e.body?.mfaToken
            if (e.code == "mfa_required" && mfaToken != null) LoginResult.MfaRequired(mfaToken) else throw e
        }
    }

    /** `POST /auth/login/mfa`: second step of the sign-in with a TOTP code. */
    public suspend fun loginMfa(mfaToken: String, code: String): LoginResult.Success {
        val body = buildJsonObject {
            put("mfaToken", mfaToken)
            put("code", code)
        }
        val response = send("auth/login/mfa", "POST", body.toBody(), AuthResponse.serializer(), authenticated = false)
        return LoginResult.Success(response.token, response.user)
    }

    /** `POST /auth/refresh`: new token with a fresh lifetime. */
    public suspend fun refresh(): String =
        send("auth/refresh", "POST", JsonObject(emptyMap()).toBody(), TokenResponse.serializer()).token

    /** `GET /whoami`: the signed-in account. */
    public suspend fun whoami(): User = get("whoami", User.serializer())

    /** `GET /vehicles`: all vehicles of the account, in the user's order. */
    public suspend fun vehicles(): List<Vehicle> = get("vehicles", ListSerializer(Vehicle.serializer()))

    // region Transport

    private suspend fun <T> get(path: String, serializer: KSerializer<T>, authenticated: Boolean = true): T =
        send(path, "GET", null, serializer, authenticated)

    private suspend fun <T> send(
        path: String,
        method: String,
        body: RequestBody?,
        serializer: KSerializer<T>,
        authenticated: Boolean = true,
    ): T {
        val url = base.resolve(path) ?: throw IllegalArgumentException("Invalid path: $path")
        val request = Request.Builder()
            .url(url)
            .method(method, body)
            .header("Accept", "application/json")
            .header("User-Agent", userAgent)
            .apply { if (authenticated) token()?.let { header("Authorization", "Bearer $it") } }
            .build()

        val (status, text) = try {
            client.newCall(request).await().use { it.code to it.body.string() }
        } catch (e: IOException) {
            throw ApiException.Network(e)
        }

        if (status !in 200..299) {
            val error = runCatching { FuelstatJson.decodeFromString(ApiErrorBody.serializer(), text) }.getOrNull()
            val exception = ApiException.Http(status, error?.error ?: "http_$status", error)
            if (authenticated && exception.isUnauthorized) onUnauthorized()
            throw exception
        }
        return try {
            FuelstatJson.decodeFromString(serializer, text)
        } catch (e: SerializationException) {
            throw ApiException.InvalidResponse(e)
        } catch (e: IllegalArgumentException) {
            throw ApiException.InvalidResponse(e)
        }
    }

    private fun JsonObject.toBody(): RequestBody = toString().toRequestBody(JSON)

    // endregion

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(
        object : Callback {
            override fun onResponse(call: Call, response: Response) = continuation.resume(response)
            override fun onFailure(call: Call, e: IOException) = continuation.resumeWithException(e)
        },
    )
}
