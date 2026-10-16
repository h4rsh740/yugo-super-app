package com.yugo.app.data.model

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val ratingCount: String,
    val deliveryTimeMinutes: Int,
    val priceForTwo: Int,
    val distanceKm: Double,
    val isPureVeg: Boolean = false,
    val offerText: String? = null,
    val imageUrl: String = "",
    val imageRes: Int = 0,
    val menuItems: List<MenuItem> = emptyList()
)

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val isVeg: Boolean,
    val category: String,
    val isBestseller: Boolean = false,
    val imageUrl: String = "",
    val imageRes: Int = 0
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)
