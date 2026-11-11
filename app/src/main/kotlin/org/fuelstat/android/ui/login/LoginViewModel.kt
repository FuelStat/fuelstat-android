package org.fuelstat.android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.fuelstat.android.R
import org.fuelstat.android.data.SessionRepository
import org.fuelstat.android.ui.UiText
import org.fuelstat.android.ui.toUiText
import org.fuelstat.core.ServerAddress
import org.fuelstat.core.api.LoginResult

data class LoginUiState(
    val server: String = ServerAddress.DEFAULT_API_BASE_URL,
    val customServer: Boolean = false,
    val email: String = "",
    val password: String = "",
    /** Set while the server waits for the 2FA code. */
    val mfaToken: String? = null,
    val code: String = "",
    val busy: Boolean = false,
    val error: UiText? = null,
) {
    val canSubmit: Boolean get() = !busy && email.isNotBlank() && password.isNotEmpty()
    val canSubmitCode: Boolean get() = !busy && code.length == 6
}

class LoginViewModel(private val sessions: SessionRepository) : ViewModel() {
    private val state = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            val last = sessions.lastServer()
            state.update {
                it.copy(
                    server = ServerAddress.displayName(last),
                    customServer = last != ServerAddress.DEFAULT_API_BASE_URL,
                )
            }
        }
    }

    fun onServerChange(value: String) = state.update { it.copy(server = value, error = null) }
    fun onEmailChange(value: String) = state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = state.update { it.copy(password = value, error = null) }
    fun onCodeChange(value: String) = state.update { s -> s.copy(code = value.filter { it.isDigit() }.take(6), error = null) }
    fun useCustomServer() = state.update { it.copy(customServer = true) }
    fun cancelMfa() = state.update { it.copy(mfaToken = null, code = "", error = null) }

    private fun baseUrl(): String? {
        val current = state.value
        return if (current.customServer) ServerAddress.apiBaseUrl(current.server) else ServerAddress.DEFAULT_API_BASE_URL
    }

    fun submit() {
        val current = state.value
        if (!current.canSubmit) return
        val baseUrl = baseUrl() ?: return state.update { it.copy(error = UiText(R.string.error_invalid_server)) }
        run {
            when (val result = sessions.publicApi(baseUrl).login(current.email.trim(), current.password)) {
                is LoginResult.Success -> sessions.signIn(baseUrl, result.token)
                is LoginResult.MfaRequired -> state.update { it.copy(mfaToken = result.mfaToken, code = "") }
            }
        }
    }

    fun submitCode() {
        val current = state.value
        val mfaToken = current.mfaToken ?: return
        if (!current.canSubmitCode) return
        val baseUrl = baseUrl() ?: return
        run {
            val result = sessions.publicApi(baseUrl).loginMfa(mfaToken, current.code)
            sessions.signIn(baseUrl, result.token)
        }
    }

    private fun run(block: suspend () -> Unit) {
        state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                block()
                state.update { it.copy(busy = false, password = "") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(busy = false, error = e.toUiText()) }
            }
        }
    }
}
