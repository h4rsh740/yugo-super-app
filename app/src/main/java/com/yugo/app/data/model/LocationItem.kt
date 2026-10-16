package com.yugo.app.data.model

data class LocationItem(
    val id: String,
    val title: String,
    val address: String,
    val latitude: Double = 28.4595,
    val longitude: Double = 77.0266,
    val type: LocationType = LocationType.RECENT
)

enum class LocationType {
    HOME, WORK, RECENT, POPULAR, AIRPORT, RAILWAY
}
