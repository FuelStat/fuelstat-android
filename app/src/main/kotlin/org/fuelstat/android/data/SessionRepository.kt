package org.fuelstat.android.data

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.fuelstat.android.BuildConfig
import org.fuelstat.core.ServerAddress
import org.fuelstat.core.api.FuelstatApi
import java.util.concurrent.TimeUnit

/** The signed-in account on one server. */
data class Session(val apiBaseUrl: String, val token: String)

sealed interface SessionState {
    data object Loading : SessionState

    /** [expired] is true when the server ended the session (token revoked or too old). */
    data class SignedOut(val expired: Boolean = false) : SessionState

    data class SignedIn(val session: Session) : SessionState
}

private val Context.sessionStore by preferencesDataStore(name = "session")

/**
 * Holds the session: server address and access token.
 *
 * The token is stored only encrypted with a Keystore key ([TokenCipher]) and is never logged.
 */
class SessionRepository(context: Context, private val scope: CoroutineScope) {
    private val store = context.applicationContext.sessionStore
    private val cipher = TokenCipher()
    private val state = MutableStateFlow<SessionState>(SessionState.Loading)

    val sessionState: StateFlow<SessionState> = state.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val userAgent = "Fuelstat-Android/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE})"

    init {
        scope.launch {
            val prefs = store.data.first()
            val baseUrl = prefs[KEY_SERVER]
            val token = prefs[KEY_TOKEN]?.let(cipher::decrypt)
            state.value = if (baseUrl != null && token != null) {
                SessionState.SignedIn(Session(baseUrl, token))
            } else {
                SessionState.SignedOut()
            }
        }
    }

    /** Server used last, to prefill the sign-in screen. */
    suspend fun lastServer(): String = store.data.first()[KEY_SERVER] ?: ServerAddress.DEFAULT_API_BASE_URL

    suspend fun signIn(apiBaseUrl: String, token: String) {
        store.edit {
            it[KEY_SERVER] = apiBaseUrl
            it[KEY_TOKEN] = cipher.encrypt(token)
        }
        state.value = SessionState.SignedIn(Session(apiBaseUrl, token))
    }

    suspend fun signOut(expired: Boolean = false) {
        store.edit { it.remove(KEY_TOKEN) }
        state.value = SessionState.SignedOut(expired)
    }

    /** Client without a token, for the sign-in. */
    fun publicApi(apiBaseUrl: String): FuelstatApi = FuelstatApi(apiBaseUrl, httpClient, userAgent)

    /** Client for the current session. Signs out when the server rejects the token. */
    fun api(): FuelstatApi {
        val session = (state.value as? SessionState.SignedIn)?.session
            ?: error("No session")
        return FuelstatApi(
            baseUrl = session.apiBaseUrl,
            client = httpClient,
            userAgent = userAgent,
            token = { (state.value as? SessionState.SignedIn)?.session?.token },
            onUnauthorized = { scope.launch { signOut(expired = true) } },
        )
    }

    private companion object {
        val KEY_SERVER = stringPreferencesKey("api_base_url")
        val KEY_TOKEN = stringPreferencesKey("token_encrypted")
    }
}
