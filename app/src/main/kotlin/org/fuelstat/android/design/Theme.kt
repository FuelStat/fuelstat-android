package org.fuelstat.android.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/** Spacing in multiples of 4 dp (docs/design.md). */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp

    /** Screen margin, gap between cards and padding inside cards. */
    val screen = m
}

private val FuelstatShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/**
 * App theme: brand colour scheme in light and dark, Material type scale (sp, follows the system font size).
 * Dynamic colour is off on purpose so the brand stays recognisable (docs/design.md).
 */
@Composable
fun FuelstatTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFuelstatColors provides if (darkTheme) DarkFuelstatColors else LightFuelstatColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            shapes = FuelstatShapes,
            content = content,
        )
    }
}

object FuelstatTheme {
    val colors: FuelstatColors
        @Composable @ReadOnlyComposable
        get() = LocalFuelstatColors.current
}
