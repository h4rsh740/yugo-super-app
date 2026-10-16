package com.yugo.app.data.model

data class ActivityItem(
    val id: String,
    val title: String,
    val serviceType: ServiceType,
    val dateFormatted: String,
    val priceFormatted: String,
    val status: String,
    val startLocation: String,
    val endLocation: String,
    val itemsSummary: String? = null,
    val imageRes: Int = 0
)

enum class ServiceType {
    CAB,
    BIKE,
    FOOD,
    PARCEL
}
