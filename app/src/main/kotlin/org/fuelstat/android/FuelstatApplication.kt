package org.fuelstat.android

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.fuelstat.android.data.SessionRepository

class FuelstatApplication : Application() {
    /** App-wide objects (manual dependency injection; small enough not to need a framework yet). */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, MainScope())
    }
}

class AppContainer(application: Application, scope: CoroutineScope) {
    val sessions = SessionRepository(application, scope)
}
