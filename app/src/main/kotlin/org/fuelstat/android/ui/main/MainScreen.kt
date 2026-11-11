package org.fuelstat.android.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.fuelstat.android.AppContainer
import org.fuelstat.android.R
import org.fuelstat.android.data.Session
import org.fuelstat.android.design.EmptyState
import org.fuelstat.android.ui.profile.ProfileScreen
import org.fuelstat.android.ui.start.StartScreen
import kotlin.reflect.KClass

@Serializable data object StartRoute

@Serializable data object EntriesRoute

@Serializable data object StatsRoute

@Serializable data object ProfileRoute

/** The four top-level destinations, same as iOS (docs/design.md). */
private enum class Destination(
    val route: Any,
    val routeClass: KClass<*>,
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
    val showsAddButton: Boolean,
) {
    START(StartRoute, StartRoute::class, R.string.nav_start, R.drawable.ic_home, showsAddButton = true),
    ENTRIES(EntriesRoute, EntriesRoute::class, R.string.nav_entries, R.drawable.ic_list, showsAddButton = true),
    STATS(StatsRoute, StatsRoute::class, R.string.nav_stats, R.drawable.ic_bar_chart, showsAddButton = false),
    PROFILE(ProfileRoute, ProfileRoute::class, R.string.nav_profile, R.drawable.ic_person, showsAddButton = false),
}

/**
 * Signed-in app shell. [NavigationSuiteScaffold] shows a navigation bar on phones and a navigation rail
 * on large screens and foldables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(container: AppContainer, session: Session) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.entries.firstOrNull { backStackEntry?.destination?.hasRoute(it.routeClass) == true }
        ?: Destination.START
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val comingSoon = stringResource(R.string.coming_soon)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Destination.entries.forEach { destination ->
                item(
                    selected = destination == current,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    // The label is visible, so the icon itself needs no description.
                    icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                    label = { Text(stringResource(destination.label)) },
                )
            }
        },
    ) {
        Scaffold(
            // The navigation suite already handles the system bars at the bottom/side.
            contentWindowInsets = WindowInsets(0),
            topBar = { TopAppBar(title = { Text(stringResource(current.label)) }) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (current.showsAddButton) {
                    FloatingActionButton(onClick = { scope.launch { snackbarHostState.showSnackbar(comingSoon) } }) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.action_add_entry))
                    }
                }
            },
        ) { padding ->
            NavHost(navController, startDestination = StartRoute, modifier = Modifier.padding(padding)) {
                composable<StartRoute> { StartScreen(container) }
                composable<EntriesRoute> {
                    EmptyState(
                        icon = R.drawable.ic_list,
                        title = stringResource(R.string.entries_title),
                        text = stringResource(R.string.entries_placeholder),
                    )
                }
                composable<StatsRoute> {
                    EmptyState(
                        icon = R.drawable.ic_bar_chart,
                        title = stringResource(R.string.stats_title),
                        text = stringResource(R.string.stats_placeholder),
                    )
                }
                composable<ProfileRoute> { ProfileScreen(container, session) }
            }
        }
    }
}
