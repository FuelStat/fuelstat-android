package org.fuelstat.android.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.fuelstat.android.AppContainer
import org.fuelstat.android.data.SessionState
import org.fuelstat.android.design.LoadingState
import org.fuelstat.android.ui.login.LoginScreen
import org.fuelstat.android.ui.main.MainScreen

/** Shows the sign-in or the app, depending on the session. */
@Composable
fun FuelstatAppRoot(container: AppContainer) {
    val state by container.sessions.sessionState.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background) {
        when (val current = state) {
            SessionState.Loading -> LoadingState()
            is SessionState.SignedOut -> LoginScreen(container, sessionExpired = current.expired)
            is SessionState.SignedIn -> MainScreen(container, current.session)
        }
    }
}
