package org.fuelstat.android.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.fuelstat.android.AppContainer
import org.fuelstat.android.R
import org.fuelstat.android.design.Spacing
import org.fuelstat.android.ui.UiText

@Composable
fun LoginScreen(container: AppContainer, sessionExpired: Boolean) {
    val viewModel = viewModel { LoginViewModel(container.sessions) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.error ?: if (sessionExpired && state.email.isEmpty()) UiText(R.string.error_session_ended) else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Spacer(Modifier.height(Spacing.xl))
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
            Text(
                stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))

            if (state.mfaToken == null) {
                CredentialsForm(state, viewModel)
            } else {
                MfaForm(state, viewModel)
            }

            if (error != null) {
                Text(
                    error.resolve(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
    }
}

@Composable
private fun CredentialsForm(state: LoginUiState, viewModel: LoginViewModel) {
    var showPassword by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = { Text(stringResource(R.string.login_email)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Username + ContentType.EmailAddress },
    )
    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::onPasswordChange,
        label = { Text(stringResource(R.string.login_password)) },
        singleLine = true,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
        trailingIcon = {
            TextButton(onClick = { showPassword = !showPassword }) {
                Text(stringResource(if (showPassword) R.string.login_hide_password else R.string.login_show_password))
            }
        },
        modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.Password },
    )

    if (state.customServer) {
        OutlinedTextField(
            value = state.server,
            onValueChange = viewModel::onServerChange,
            label = { Text(stringResource(R.string.login_server)) },
            supportingText = { Text(stringResource(R.string.login_server_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        TextButton(onClick = viewModel::useCustomServer) { Text(stringResource(R.string.login_server_change)) }
    }

    SubmitButton(R.string.login_submit, enabled = state.canSubmit, busy = state.busy, onClick = viewModel::submit)
}

@Composable
private fun MfaForm(state: LoginUiState, viewModel: LoginViewModel) {
    Text(stringResource(R.string.login_mfa_title), style = MaterialTheme.typography.titleLarge)
    Text(stringResource(R.string.login_mfa_text), style = MaterialTheme.typography.bodyMedium)
    OutlinedTextField(
        value = state.code,
        onValueChange = viewModel::onCodeChange,
        label = { Text(stringResource(R.string.login_mfa_code)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { viewModel.submitCode() }),
        modifier = Modifier.fillMaxWidth().semantics { contentType = ContentType.SmsOtpCode },
    )
    SubmitButton(R.string.login_mfa_submit, enabled = state.canSubmitCode, busy = state.busy, onClick = viewModel::submitCode)
    TextButton(onClick = viewModel::cancelMfa, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.login_mfa_cancel))
    }
}

@Composable
private fun SubmitButton(label: Int, enabled: Boolean, busy: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(52.dp)) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(label))
        }
    }
}
