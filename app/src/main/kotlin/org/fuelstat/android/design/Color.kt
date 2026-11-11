package org.fuelstat.android.design

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand tokens, same values as the iOS asset catalog (docs/design.md there).
// Screens use MaterialTheme.colorScheme or FuelstatTheme.colors, never these values directly.

private val Petrol = Color(0xFF0A717A)
private val Teal = Color(0xFF5ED3CF)
private val Navy = Color(0xFF0A1C33)
private val NavyLight = Color(0xFFDCE8F5)

internal val LightColors = lightColorScheme(
    primary = Petrol,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F1F2),
    onPrimaryContainer = Navy,
    secondary = Navy,
    onSecondary = Color.White,
    background = Color(0xFFF2F6F8),
    onBackground = Navy,
    surface = Color(0xFFF2F6F8),
    onSurface = Navy,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFE2F1F2),
    error = Color(0xFFB93A0C),
)

internal val DarkColors = darkColorScheme(
    primary = Teal,
    onPrimary = Navy,
    primaryContainer = Color(0xFF22303A),
    onPrimaryContainer = NavyLight,
    secondary = NavyLight,
    onSecondary = Navy,
    background = Color(0xFF0B111A),
    onBackground = NavyLight,
    surface = Color(0xFF0B111A),
    onSurface = NavyLight,
    surfaceContainerLowest = Color(0xFF1A2230),
    surfaceContainerLow = Color(0xFF1A2230),
    surfaceContainer = Color(0xFF22303A),
    error = Color(0xFFFF8A5B),
)

/** Purpose colours that Material has no slot for: status of due dates and energy types. */
@Immutable
data class FuelstatColors(
    val statusOk: Color,
    val statusSoon: Color,
    val statusOverdue: Color,
    val energyElectric: Color,
    val energyPetrol: Color,
    val energyDiesel: Color,
    val energyGas: Color,
    val energyHydrogen: Color,
)

internal val LightFuelstatColors = FuelstatColors(
    statusOk = Color(0xFF18723A),
    statusSoon = Color(0xFF8A5A00),
    statusOverdue = Color(0xFFB93A0C),
    energyElectric = Color(0xFF0F883B),
    energyPetrol = Color(0xFF8A6D00),
    energyDiesel = Color(0xFFC0501A),
    energyGas = Color(0xFF2563EB),
    energyHydrogen = Color(0xFF0E7490),
)

internal val DarkFuelstatColors = FuelstatColors(
    statusOk = Color(0xFF4ADE80),
    statusSoon = Color(0xFFF5C542),
    statusOverdue = Color(0xFFFF8A5B),
    energyElectric = Color(0xFF3DD17A),
    energyPetrol = Color(0xFFF2C230),
    energyDiesel = Color(0xFFFF9A52),
    energyGas = Color(0xFF6CA8FF),
    energyHydrogen = Color(0xFF3FD6EE),
)

internal val LocalFuelstatColors = staticCompositionLocalOf { LightFuelstatColors }
