package com.yugo.app.data.api

import com.google.gson.annotations.SerializedName

// --- Auth DTOs ---

data class RequestOtpRequest(
    val phone: String
)

data class RequestOtpResponse(
    val success: Boolean,
    val message: String,
    val phone: String?,
    val testOtp: String? = null
)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserProfileDto?
)

data class UserProfileDto(
    val id: String,
    val phone: String,
    val name: String,
    val email: String? = null,
    val rating: Double = 4.9,
    val yugoCashBalance: Double = 500.0,
    val isPhoneVerified: Boolean = true,
    val savedPlaces: List<SavedPlaceDto> = emptyList(),
    val emergencyContacts: List<EmergencyContactDto> = emptyList(),
    val preferences: NotificationPreferencesDto? = null
)

data class SavedPlaceDto(
    val id: String? = null,
    val name: String,
    val address: String,
    val label: String = "OTHER"
)

data class CreateAddressRequest(
    val title: String,
    val address: String,
    val label: String = "OTHER",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isDefault: Boolean = false
)

data class EmergencyContactDto(
    val id: String? = null,
    val name: String,
    val phone: String,
    val relationship: String = "Family"
)

data class CreateEmergencyContactRequest(
    val name: String,
    val phone: String,
    val relationship: String = "Family"
)

data class NotificationPreferencesDto(
    val rideUpdates: Boolean = true,
    val foodUpdates: Boolean = true,
    val parcelUpdates: Boolean = true,
    val promotional: Boolean = false,
    val pushEnabled: Boolean = true,
    val smsEnabled: Boolean = true
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val success: Boolean,
    val accessToken: String?
)

data class UpdateProfileRequest(
    val name: String?,
    val email: String?
)

data class SimpleSuccessResponse(
    val success: Boolean,
    val message: String?
)

// --- Health ---

data class HealthResponse(
    val status: String,
    val service: String,
    val version: String,
    val database: String? = null
)

// --- Ride DTOs ---

data class RideOptionsResponse(
    val success: Boolean,
    val distanceKm: Double,
    val options: List<RideOptionDto>
)

data class RideOptionDto(
    val id: String,
    val name: String,
    val category: String,
    val capacity: Int,
    val etaMinutes: Int,
    val fareAmount: Double,
    val description: String,
    val isRecommended: Boolean,
    val imageKey: String?
)

data class DriversResponse(
    val success: Boolean,
    val count: Int,
    val drivers: List<DriverInfoDto>
)

data class DriverInfoDto(
    val id: String,
    val name: String,
    val rating: Double,
    val totalTrips: Int,
    val vehicleModel: String,
    val vehicleColor: String,
    val plateNumber: String,
    val phoneNumber: String,
    val otpPin: String,
    val latitude: Double,
    val longitude: Double
)

data class CreateRideRequest(
    val pickup: Any?,
    val destination: Any?,
    val rideOption: Any?,
    val paymentMethod: String = "Cash"
)

data class BookingResponseDto(
    val success: Boolean,
    val message: String?,
    val booking: BookingDetailDto?
)

data class BookingDetailDto(
    val bookingId: String,
    val status: String,
    val estimatedEtaMinutes: Int,
    val otpPin: String?,
    val driver: DriverInfoDto?,
    val rideOption: RideOptionDto? = null
)

data class UpdateRideStatusRequest(
    val status: String
)

// --- Food DTOs ---

data class RestaurantsResponse(
    val success: Boolean,
    val count: Int,
    val restaurants: List<RestaurantDto>
)

data class RestaurantDto(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val ratingCount: String,
    val deliveryTimeMinutes: Int,
    val priceForTwo: Int,
    val distanceKm: Double,
    val isPureVeg: Boolean,
    val offerText: String?,
    val imageKey: String?,
    val menuItems: List<MenuItemDto>?
)

data class MenuItemDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isVeg: Boolean,
    val category: String,
    val isBestseller: Boolean,
    val imageKey: String?
)

data class OrderItemDto(
    val menuItemId: String,
    val quantity: Int
)

data class CalculateBillRequest(
    val restaurantId: String,
    val items: List<OrderItemDto>,
    val couponCode: String? = null
)

data class CalculateBillResponse(
    val success: Boolean,
    val bill: BillBreakdownDto?
)

data class BillBreakdownDto(
    val subtotal: Double,
    val deliveryFee: Double,
    val packagingFee: Double,
    val taxes: Double,
    val discount: Double,
    val totalAmount: Double
)

data class PlaceOrderRequest(
    val restaurantId: String,
    val cartItems: List<Any>? = null,
    val items: List<OrderItemDto>? = null,
    val deliveryAddress: String,
    val totalAmount: Double? = null,
    val paymentMethod: String = "Online",
    val couponCode: String? = null
)

data class FoodOrderResponseDto(
    val success: Boolean,
    val message: String?,
    val order: FoodOrderDetailDto?
)

data class FoodOrderDetailDto(
    val orderId: String,
    val restaurantId: String,
    val restaurantName: String?,
    val status: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val estimatedDeliveryMinutes: Int,
    val placedAt: String?
)

data class UpdateOrderStatusRequest(
    val status: String
)

// --- Parcel DTOs ---

data class EstimateParcelRequest(
    val weightKg: Float,
    val distanceKm: Float = 4.5f,
    val category: String = "DOCUMENTS"
)

data class EstimateParcelResponse(
    val success: Boolean,
    val estimatedPrice: Double,
    val breakdown: ParcelBreakdownDto? = null
)

data class ParcelBreakdownDto(
    val baseFare: Double,
    val weightCharge: Double,
    val distanceCharge: Double,
    val total: Double
)

data class BookParcelRequest(
    val recipientName: String,
    val recipientPhone: String,
    val recipientAddress: String,
    val weightKg: Float,
    val category: String,
    val instructions: String? = null,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val pickupAddress: String? = null,
    val paymentMethod: String = "Online"
)

data class BookParcelResponseDto(
    val success: Boolean,
    val message: String?,
    val parcel: ParcelDetailDto?
)

data class ParcelDetailDto(
    val trackingId: String,
    val recipientName: String,
    val recipientPhone: String,
    val recipientAddress: String,
    val category: String,
    val weightKg: Float,
    val status: String,
    val price: Double,
    val deliveryPin: String?,
    val riderName: String?,
    val riderPhone: String?,
    val estimatedPickupMinutes: Int? = 12,
    val estimatedDeliveryMinutes: Int? = 35
)

// --- Wallet & Payments ---

data class TopupWalletRequest(
    val amount: Double,
    val method: String = "UPI"
)

data class TopupWalletResponse(
    val success: Boolean,
    val newBalance: Double?,
    val message: String?
)

// --- Activities & Locations ---

data class ActivitiesResponse(
    val success: Boolean,
    val count: Int,
    val activities: List<ActivityItemDto>
)

data class ActivityItemDto(
    val id: String,
    val title: String,
    val serviceType: String,
    val dateFormatted: String,
    val priceFormatted: String,
    val status: String,
    val startLocation: String,
    val endLocation: String,
    val itemsSummary: String?,
    val imageKey: String?,
    val referenceId: String? = null
)

data class LocationsResponse(
    val success: Boolean,
    val count: Int,
    val locations: List<LocationItemDto>
)

data class LocationItemDto(
    val id: String,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: String
)
