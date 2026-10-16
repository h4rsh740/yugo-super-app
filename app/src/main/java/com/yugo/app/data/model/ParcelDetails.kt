package com.yugo.app.data.model

data class ParcelDetails(
    val pickupAddress: String,
    val senderName: String,
    val senderPhone: String,
    val recipientAddress: String,
    val recipientName: String,
    val recipientPhone: String,
    val packageCategory: ParcelCategory,
    val weightKg: Float,
    val instructions: String = "",
    val estimatedPrice: Double = 0.0
)

enum class ParcelCategory {
    DOCUMENTS,
    ELECTRONICS,
    CLOTHING,
    FOOD_GROCERY,
    GIFTS_FLOWERS,
    OTHER
}

enum class ParcelStatus {
    CREATED,
    COURIER_ASSIGNED,
    PICKED_UP,
    IN_TRANSIT,
    DELIVERED
}
