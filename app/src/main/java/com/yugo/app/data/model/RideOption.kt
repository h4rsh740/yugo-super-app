package com.yugo.app.data.model

data class RideOption(
    val id: String,
    val name: String,
    val category: RideCategory,
    val capacity: Int,
    val etaMinutes: Int,
    val fareAmount: Double,
    val description: String,
    val isRecommended: Boolean = false,
    val iconResName: String = "",
    val imageRes: Int = 0
)

enum class RideCategory {
    AUTO, MOTO, GO, PREMIER, XL
}
