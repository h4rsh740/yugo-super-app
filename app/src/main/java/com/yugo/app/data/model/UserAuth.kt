package com.yugo.app.data.model

data class User(
    val id: String,
    val phone: String,
    val name: String,
    val email: String? = null,
    val rating: Double = 4.9,
    val yugoCashBalance: Double = 500.0,
    val isPhoneVerified: Boolean = true,
    val savedPlaces: List<SavedPlace> = emptyList(),
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val preferences: NotificationPreferences = NotificationPreferences()
)

data class SavedPlace(
    val id: String = "",
    val name: String,
    val address: String,
    val label: String = "OTHER"
)

data class EmergencyContact(
    val id: String = "",
    val name: String,
    val phone: String,
    val relationship: String = "Family"
)

data class NotificationPreferences(
    val rideUpdates: Boolean = true,
    val foodUpdates: Boolean = true,
    val parcelUpdates: Boolean = true,
    val promotional: Boolean = false,
    val pushEnabled: Boolean = true,
    val smsEnabled: Boolean = true
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

enum class PaymentType {
    WALLET,
    UPI,
    CARD,
    CASH
}

data class PaymentMethodItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: PaymentType,
    val isSelected: Boolean = false
)
