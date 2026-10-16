package com.yugo.app.data.local

import com.yugo.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YugoDataStore {

    private val _walletBalance = MutableStateFlow(1240.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    private val _activeRide = MutableStateFlow<ActiveRideState?>(null)
    val activeRide: StateFlow<ActiveRideState?> = _activeRide.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "usr_9981",
            name = "Harsh Singh",
            phone = "+91 98765 12345",
            email = "harsh.singh@yugo.app",
            rating = 4.95,
            totalRides = 142
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun addWalletMoney(amount: Double) {
        _walletBalance.value += amount
    }

    fun deductWalletMoney(amount: Double): Boolean {
        if (_walletBalance.value >= amount) {
            _walletBalance.value -= amount
            return true
        }
        return false
    }

    fun setActiveRide(rideState: ActiveRideState?) {
        _activeRide.value = rideState
    }
}

data class ActiveRideState(
    val rideId: String,
    val selectedOption: RideOption,
    val pickup: LocationItem,
    val destination: LocationItem,
    val driver: DriverInfo,
    val status: RideStatus
)

data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val rating: Double,
    val totalRides: Int
)
