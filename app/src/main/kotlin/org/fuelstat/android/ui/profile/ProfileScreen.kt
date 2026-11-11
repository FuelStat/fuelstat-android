package org.fuelstat.android.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.fuelstat.android.AppContainer
import org.fuelstat.android.BuildConfig
import org.fuelstat.android.R
import org.fuelstat.android.data.Session
import org.fuelstat.core.ServerAddress
import org.fuelstat.core.api.User

@Composable
fun ProfileScreen(container: AppContainer, session: Session) {
    val scope = rememberCoroutineScope()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val user by produceState<User?>(initialValue = null, session) {
        value = try {
            container.sessions.api().whoami()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null // Shown as "Wird geladen …"; errors are handled on Start.
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ListItem(
            headlineContent = { Text(user?.displayName ?: stringResource(R.string.loading)) },
            supportingContent = { user?.let { if (it.displayName != it.email) Text(it.email) } },
            overlineContent = { Text(stringResource(R.string.profile_account)) },
            leadingContent = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
        )
        ListItem(
            overlineContent = { Text(stringResource(R.string.profile_server)) },
            headlineContent = { Text(ServerAddress.displayName(session.apiBaseUrl)) },
        )
        ListItem(
            overlineContent = { Text(stringResource(R.string.profile_version)) },
            headlineContent = { Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})") },
        )
        HorizontalDivider()
        TextButton(onClick = { confirmLogout = true }) {
            Icon(painterResource(R.drawable.ic_logout), contentDescription = null)
            Text(stringResource(R.string.profile_logout), color = MaterialTheme.colorScheme.error)
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { scope.launch { container.sessions.signOut() } }) {
                    Text(stringResource(R.string.profile_logout))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
