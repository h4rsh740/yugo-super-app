package com.yugo.app.data.model

data class DriverInfo(
    val id: String,
    val name: String,
    val rating: Double,
    val totalTrips: Int,
    val vehicleModel: String,
    val vehicleColor: String,
    val plateNumber: String,
    val phoneNumber: String,
    val otpPin: String,
    val latitude: Double = 28.4610,
    val longitude: Double = 77.0280
)

enum class RideStatus {
    SEARCHING,
    DRIVER_ASSIGNED,
    DRIVER_ARRIVING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
