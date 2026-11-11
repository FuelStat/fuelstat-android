package org.fuelstat.android.ui.start

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.fuelstat.android.AppContainer
import org.fuelstat.android.R
import org.fuelstat.android.design.EmptyState
import org.fuelstat.android.design.FuelstatTheme
import org.fuelstat.android.design.LoadingState
import org.fuelstat.android.design.Spacing
import org.fuelstat.core.api.FuelType
import org.fuelstat.core.api.Vehicle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(container: AppContainer) {
    val viewModel = viewModel { StartViewModel(container.sessions) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val vehicles = state.vehicles
    val error = state.error

    when {
        vehicles == null && error != null -> EmptyState(
            icon = R.drawable.ic_cloud_off,
            title = error.resolve(),
            actionLabel = stringResource(R.string.retry),
            onAction = viewModel::refresh,
        )
        vehicles == null -> LoadingState()
        else -> PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (vehicles.isEmpty()) {
                EmptyState(
                    icon = R.drawable.ic_car,
                    title = stringResource(R.string.start_empty_title),
                    text = stringResource(R.string.start_empty_text),
                )
            } else {
                VehicleList(vehicles)
            }
        }
    }
}

@Composable
private fun VehicleList(vehicles: List<Vehicle>) {
    LazyColumn(
        contentPadding = PaddingValues(Spacing.screen),
        verticalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                stringResource(R.string.start_vehicles),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
        }
        items(vehicles, key = { it.id }) { VehicleCard(it) }
    }
}

/** First version of the vehicle card; the hero with photo and key figures follows (docs/design.md). */
@Composable
private fun VehicleCard(vehicle: Vehicle) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_car), contentDescription = null, tint = energyColor(vehicle.fuelType))
                }
            }
            Column(Modifier.padding(start = Spacing.m).weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(vehicle.name, style = MaterialTheme.typography.titleMedium)
                val details = listOfNotNull(vehicle.subtitle, stringResource(vehicle.fuelType.label))
                Text(
                    details.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                vehicle.licensePlate?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        stringResource(R.string.vehicle_plate, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@get:StringRes
val FuelType.label: Int
    get() = when (this) {
        FuelType.DIESEL -> R.string.fuel_diesel
        FuelType.E5 -> R.string.fuel_e5
        FuelType.E10 -> R.string.fuel_e10
        FuelType.LPG -> R.string.fuel_lpg
        FuelType.CNG -> R.string.fuel_cng
        FuelType.ELECTRIC -> R.string.fuel_electric
        FuelType.HYBRID -> R.string.fuel_hybrid
        FuelType.PHEV -> R.string.fuel_phev
        FuelType.HYDROGEN -> R.string.fuel_hydrogen
        FuelType.OTHER -> R.string.fuel_other
    }

@Composable
private fun energyColor(type: FuelType): Color = with(FuelstatTheme.colors) {
    when (type) {
        FuelType.ELECTRIC, FuelType.PHEV -> energyElectric
        FuelType.DIESEL -> energyDiesel
        FuelType.E5, FuelType.E10, FuelType.HYBRID -> energyPetrol
        FuelType.LPG, FuelType.CNG -> energyGas
        FuelType.HYDROGEN -> energyHydrogen
        FuelType.OTHER -> MaterialTheme.colorScheme.primary
    }
}
