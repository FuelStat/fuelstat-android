package org.fuelstat.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.fuelstat.android.design.FuelstatTheme
import org.fuelstat.android.ui.FuelstatAppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as FuelstatApplication).container
        setContent {
            FuelstatTheme {
                FuelstatAppRoot(container)
            }
        }
    }
}
