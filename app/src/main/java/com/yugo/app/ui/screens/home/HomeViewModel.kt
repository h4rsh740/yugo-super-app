package com.yugo.app.ui.screens.home

import androidx.lifecycle.ViewModel
import com.yugo.app.data.model.LocationItem
import com.yugo.app.data.repository.YugoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val pickupLocation: LocationItem = LocationItem("curr", "Current Location", "DLF Phase 2, Gurugram"),
    val recentLocations: List<LocationItem> = emptyList(),
    val activeServiceType: String = "CAB",
    val activeRideId: String? = null,
    val activeRideStatusText: String? = null
)

class HomeViewModel(
    private val repository: YugoRepository = YugoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            recentLocations = repository.recentLocations
        )
    }

    fun selectPickup(location: LocationItem) {
        _uiState.value = _uiState.value.copy(pickupLocation = location)
    }
}
