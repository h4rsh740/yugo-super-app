package com.yugo.app.ui.screens.ride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.model.*
import com.yugo.app.data.repository.YugoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RideBookingUiState(
    val pickupLocation: LocationItem = LocationItem("curr", "Sector 54, Gurugram", "Sector 54, Golf Course Road, Gurugram", 28.4382, 77.1082, LocationType.HOME),
    val destinationLocation: LocationItem? = null,
    val rideOptions: List<RideOption> = emptyList(),
    val selectedOption: RideOption? = null,
    val paymentMethod: String = "Cash",
    val promoCode: String? = null,
    val rideStatus: RideStatus = RideStatus.SEARCHING,
    val assignedDriver: DriverInfo? = null,
    val etaMinutesRemaining: Int = 3,
    val bookingId: String? = null,
    val isBookingInProgress: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
)

class RideBookingViewModel(
    private val repository: YugoRepository = YugoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideBookingUiState())
    val uiState: StateFlow<RideBookingUiState> = _uiState.asStateFlow()

    private var statusPollingJob: Job? = null

    init {
        loadRideOptions(5.8)
    }

    fun loadRideOptions(distanceKm: Double = 5.8) {
        viewModelScope.launch {
            val options = repository.getAvailableRideOptions(distanceKm)
            _uiState.value = _uiState.value.copy(
                rideOptions = options,
                selectedOption = _uiState.value.selectedOption ?: options.firstOrNull { it.isRecommended } ?: options.firstOrNull()
            )
        }
    }

    fun setPickup(location: LocationItem) {
        _uiState.value = _uiState.value.copy(pickupLocation = location)
    }

    fun setDestination(location: LocationItem) {
        _uiState.value = _uiState.value.copy(
            destinationLocation = location,
            selectedOption = _uiState.value.selectedOption ?: repository.getDefaultRideOptions().firstOrNull { it.isRecommended }
        )
        loadRideOptions(distanceKm = 6.2)
    }

    fun selectRideOption(option: RideOption) {
        _uiState.value = _uiState.value.copy(selectedOption = option)
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun confirmBooking(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBookingInProgress = true,
                rideStatus = RideStatus.SEARCHING,
                errorMessage = null
            )

            val pickup = _uiState.value.pickupLocation
            val dest = _uiState.value.destinationLocation ?: LocationItem("dest_default", "Cyber City Phase 2", "DLF Cyber City, Gurugram", 28.4950, 77.0895, LocationType.WORK)
            val option = _uiState.value.selectedOption ?: repository.getDefaultRideOptions().first()

            val result = repository.bookRideOnServer(pickup, dest, option, _uiState.value.paymentMethod)
            if (result is ApiResult.Success) {
                val booking = result.data
                val driverDto = booking.driver
                val driver = if (driverDto != null) {
                    DriverInfo(
                        id = driverDto.id,
                        name = driverDto.name,
                        rating = driverDto.rating,
                        totalTrips = driverDto.totalTrips,
                        vehicleModel = driverDto.vehicleModel,
                        vehicleColor = driverDto.vehicleColor,
                        plateNumber = driverDto.plateNumber,
                        phoneNumber = driverDto.phoneNumber,
                        otpPin = booking.otpPin ?: "4819",
                        latitude = driverDto.latitude,
                        longitude = driverDto.longitude
                    )
                } else {
                    repository.fetchNearbyDrivers(pickup.latitude, pickup.longitude).firstOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    bookingId = booking.bookingId,
                    rideStatus = RideStatus.DRIVER_ASSIGNED,
                    assignedDriver = driver,
                    etaMinutesRemaining = booking.estimatedEtaMinutes,
                    isBookingInProgress = false
                )
                onSuccess()

                // Begin realistic lifecycle transitions
                startRideLifecycle(booking.bookingId)
            } else if (result is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isBookingInProgress = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun startRideLifecycle(bookingId: String) {
        statusPollingJob?.cancel()
        statusPollingJob = viewModelScope.launch {
            // 1. DRIVER_ASSIGNED (driver matched)
            delay(4000)
            if (_uiState.value.rideStatus == RideStatus.CANCELLED) return@launch

            // 2. DRIVER_ARRIVING
            repository.updateRideStatus(bookingId, "DRIVER_ARRIVING")
            _uiState.value = _uiState.value.copy(
                rideStatus = RideStatus.DRIVER_ARRIVING,
                etaMinutesRemaining = 1
            )

            delay(4000)
            if (_uiState.value.rideStatus == RideStatus.CANCELLED) return@launch

            // 3. TRIP_STARTED (PIN entered & ride on the way)
            repository.updateRideStatus(bookingId, "TRIP_STARTED")
            _uiState.value = _uiState.value.copy(
                rideStatus = RideStatus.IN_PROGRESS,
                etaMinutesRemaining = 8
            )

            delay(5000)
            if (_uiState.value.rideStatus == RideStatus.CANCELLED) return@launch

            // 4. COMPLETED
            repository.updateRideStatus(bookingId, "COMPLETED")
            _uiState.value = _uiState.value.copy(
                rideStatus = RideStatus.COMPLETED,
                etaMinutesRemaining = 0,
                isCompleted = true
            )
        }
    }

    fun cancelRide(onCancelled: () -> Unit = {}) {
        val id = _uiState.value.bookingId
        statusPollingJob?.cancel()
        viewModelScope.launch {
            if (id != null) {
                repository.cancelRide(id)
            }
            _uiState.value = _uiState.value.copy(
                rideStatus = RideStatus.CANCELLED,
                assignedDriver = null,
                destinationLocation = null,
                bookingId = null
            )
            onCancelled()
        }
    }

    fun resetRide() {
        statusPollingJob?.cancel()
        _uiState.value = RideBookingUiState()
        loadRideOptions(5.8)
    }
}
