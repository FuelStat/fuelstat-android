package org.fuelstat.android.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.fuelstat.android.R
import org.fuelstat.core.api.ApiException

/** A user-facing text from resources, resolved in the UI (so ViewModels stay free of Context). */
data class UiText(@param:StringRes val id: Int, val args: List<Any> = emptyList()) {
    @Composable
    fun resolve(): String = stringResource(id, *args.toTypedArray())
}

/** Maps API error codes (docs/api.md) to German messages. The API decides; the app only explains. */
fun Throwable.toUiText(): UiText = when (this) {
    is ApiException.Network -> UiText(R.string.error_network)
    is ApiException.InvalidResponse -> UiText(R.string.error_invalid_response)
    is ApiException.Http -> when (code) {
        "missing_credentials" -> UiText(R.string.error_missing_credentials)
        "invalid_credentials" -> UiText(R.string.error_invalid_credentials)
        "invalid_mfa_code" -> UiText(R.string.error_invalid_mfa_code)
        "invalid_mfa_token" -> UiText(R.string.error_invalid_mfa_token)
        "too_many_attempts" -> UiText(R.string.error_too_many_attempts)
        else -> when {
            status == 401 -> UiText(R.string.error_session_ended)
            status >= 500 -> UiText(R.string.error_server)
            else -> UiText(R.string.error_generic, listOf(code))
        }
    }
    else -> UiText(R.string.error_generic, listOf(javaClass.simpleName))
}
