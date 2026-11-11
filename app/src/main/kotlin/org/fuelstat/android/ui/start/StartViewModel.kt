package org.fuelstat.android.ui.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.fuelstat.android.data.SessionRepository
import org.fuelstat.android.ui.UiText
import org.fuelstat.android.ui.toUiText
import org.fuelstat.core.api.Vehicle

data class StartUiState(
    val vehicles: List<Vehicle>? = null,
    val refreshing: Boolean = false,
    val error: UiText? = null,
)

class StartViewModel(private val sessions: SessionRepository) : ViewModel() {
    private val state = MutableStateFlow(StartUiState())
    val uiState: StateFlow<StartUiState> = state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        state.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            try {
                val vehicles = sessions.api().vehicles()
                state.update { it.copy(vehicles = vehicles, refreshing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                state.update { it.copy(refreshing = false, error = e.toUiText()) }
            }
        }
    }
}
