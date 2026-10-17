package com.yugo.app.data.api

import com.yugo.app.R
import com.yugo.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YugoApiService {

    suspend fun checkBackendHealth(): ApiResult<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getHealth()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Backend error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Could not connect to backend: ${e.localizedMessage}")
        }
    }

    // --- Authentication & Profile ---

    suspend fun requestOtp(phone: String): ApiResult<RequestOtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.requestOtp(RequestOtpRequest(phone))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errStr = response.errorBody()?.string()
                val parsed = try {
                    org.json.JSONObject(errStr ?: "").optString("message", "Failed to request OTP (${response.code()})")
                } catch (e: Exception) {
                    errStr ?: "Failed to request OTP (${response.code()})"
                }
                ApiResult.Error(parsed)
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun verifyOtp(phone: String, otp: String): ApiResult<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.verifyOtp(VerifyOtpRequest(phone, otp))
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val errStr = response.errorBody()?.string()
                val parsed = try {
                    org.json.JSONObject(errStr ?: "").optString("message", "Invalid OTP code (${response.code()})")
                } catch (e: Exception) {
                    errStr ?: "Invalid OTP code (${response.code()})"
                }
                ApiResult.Error(parsed)
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun fetchProfile(): ApiResult<UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getProfile()
            if (response.isSuccessful && response.body()?.user != null) {
                ApiResult.Success(response.body()!!.user!!)
            } else {
                ApiResult.Error("Failed to fetch profile (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun updateProfile(name: String, email: String): ApiResult<UserProfileDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.updateProfile(UpdateProfileRequest(name, email))
            if (response.isSuccessful && response.body()?.user != null) {
                ApiResult.Success(response.body()!!.user!!)
            } else {
                ApiResult.Error("Failed to update profile (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun addSavedAddress(title: String, address: String, label: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.addAddress(CreateAddressRequest(title, address, label))
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("Failed to add address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteSavedAddress(id: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.deleteAddress(id)
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("Failed to remove address")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun addEmergencyContact(name: String, phone: String, relationship: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.addEmergencyContact(CreateEmergencyContactRequest(name, phone, relationship))
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("Failed to add emergency contact")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteEmergencyContact(id: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.deleteEmergencyContact(id)
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("Failed to remove emergency contact")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updatePreferences(prefs: NotificationPreferencesDto): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.updatePreferences(prefs)
            if (response.isSuccessful) {
                ApiResult.Success(true)
            } else {
                ApiResult.Error("Failed to update preferences")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun topupWallet(amount: Double, method: String = "UPI"): ApiResult<Double> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.topupWallet(TopupWalletRequest(amount, method))
            if (response.isSuccessful && response.body()?.newBalance != null) {
                ApiResult.Success(response.body()!!.newBalance!!)
            } else {
                ApiResult.Error("Top-up failed (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // --- Rides ---

    suspend fun getRideOptions(distanceKm: Double = 5.8): ApiResult<List<RideOption>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getRideOptions(distanceKm)
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.options.map { dto ->
                    RideOption(
                        id = dto.id,
                        name = dto.name,
                        category = when (dto.category.uppercase()) {
                            "AUTO" -> RideCategory.AUTO
                            "MOTO" -> RideCategory.MOTO
                            "GO" -> RideCategory.GO
                            "PREMIER" -> RideCategory.PREMIER
                            "XL" -> RideCategory.XL
                            else -> RideCategory.GO
                        },
                        capacity = dto.capacity,
                        etaMinutes = dto.etaMinutes,
                        fareAmount = dto.fareAmount,
                        description = dto.description,
                        isRecommended = dto.isRecommended,
                        imageRes = resolveImageRes(dto.imageKey)
                    )
                }
                return@withContext ApiResult.Success(list)
            }
        } catch (e: Exception) {
            // fallback handled at repository level
        }
        ApiResult.Error("Could not fetch ride fares")
    }

    suspend fun getNearbyDrivers(lat: Double, lng: Double): ApiResult<List<DriverInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getNearbyDrivers(lat, lng)
            if (response.isSuccessful && response.body()?.success == true) {
                val drivers = response.body()!!.drivers.map { dto ->
                    DriverInfo(
                        id = dto.id,
                        name = dto.name,
                        rating = dto.rating,
                        totalTrips = dto.totalTrips,
                        vehicleModel = dto.vehicleModel,
                        vehicleColor = dto.vehicleColor,
                        plateNumber = dto.plateNumber,
                        phoneNumber = dto.phoneNumber,
                        otpPin = dto.otpPin,
                        latitude = dto.latitude,
                        longitude = dto.longitude
                    )
                }
                return@withContext ApiResult.Success(drivers)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not fetch drivers")
    }

    suspend fun createRideBooking(
        pickup: LocationItem,
        destination: LocationItem,
        rideOption: RideOption,
        paymentMethod: String = "Cash"
    ): ApiResult<BookingDetailDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateRideRequest(
                pickup = mapOf("title" to pickup.title, "address" to pickup.address, "latitude" to pickup.latitude, "longitude" to pickup.longitude),
                destination = mapOf("title" to destination.title, "address" to destination.address, "latitude" to destination.latitude, "longitude" to destination.longitude),
                rideOption = mapOf("category" to rideOption.category.name, "name" to rideOption.name, "fareAmount" to rideOption.fareAmount, "etaMinutes" to rideOption.etaMinutes),
                paymentMethod = paymentMethod
            )
            val response = ApiClient.api.bookRide(request)
            if (response.isSuccessful && response.body()?.booking != null) {
                return@withContext ApiResult.Success(response.body()!!.booking!!)
            } else {
                return@withContext ApiResult.Error("Booking failed: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error booking ride: ${e.localizedMessage}")
        }
    }

    suspend fun getRideTracking(bookingId: String): ApiResult<BookingDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getRideTracking(bookingId)
            if (response.isSuccessful && response.body()?.booking != null) {
                ApiResult.Success(response.body()!!.booking!!)
            } else {
                ApiResult.Error("Booking not found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateRideStatus(bookingId: String, status: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.updateRideStatus(bookingId, UpdateRideStatusRequest(status))
            if (response.isSuccessful) ApiResult.Success(true) else ApiResult.Error("Status update failed")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun cancelRide(bookingId: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.cancelRide(bookingId)
            if (response.isSuccessful) ApiResult.Success(true) else ApiResult.Error("Cancel failed")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // --- Food Delivery ---

    suspend fun fetchRestaurants(cuisine: String? = null, search: String? = null): ApiResult<List<Restaurant>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getRestaurants(cuisine, search)
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.restaurants.map { dto ->
                    Restaurant(
                        id = dto.id,
                        name = dto.name,
                        cuisine = dto.cuisine,
                        rating = dto.rating,
                        ratingCount = dto.ratingCount,
                        deliveryTimeMinutes = dto.deliveryTimeMinutes,
                        priceForTwo = dto.priceForTwo,
                        distanceKm = dto.distanceKm,
                        isPureVeg = dto.isPureVeg,
                        offerText = dto.offerText,
                        imageUrl = "",
                        imageRes = resolveImageRes(dto.imageKey),
                        menuItems = dto.menuItems?.map { m ->
                            MenuItem(
                                id = m.id,
                                name = m.name,
                                description = m.description,
                                price = m.price,
                                isVeg = m.isVeg,
                                category = m.category,
                                isBestseller = m.isBestseller,
                                imageRes = resolveImageRes(m.imageKey)
                            )
                        } ?: emptyList()
                    )
                }
                return@withContext ApiResult.Success(list)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not fetch restaurants from server")
    }

    suspend fun calculateFoodBill(restaurantId: String, cartItems: List<CartItem>, couponCode: String? = null): ApiResult<BillBreakdownDto> = withContext(Dispatchers.IO) {
        try {
            val itemsDto = cartItems.map { OrderItemDto(menuItemId = it.menuItem.id, quantity = it.quantity) }
            val response = ApiClient.api.calculateFoodBill(CalculateBillRequest(restaurantId, itemsDto, couponCode))
            if (response.isSuccessful && response.body()?.bill != null) {
                ApiResult.Success(response.body()!!.bill!!)
            } else {
                ApiResult.Error("Bill calculation failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun placeFoodOrder(
        restaurantId: String,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        paymentMethod: String = "Online",
        couponCode: String? = null
    ): ApiResult<FoodOrderDetailDto> = withContext(Dispatchers.IO) {
        try {
            val itemsDto = cartItems.map { OrderItemDto(menuItemId = it.menuItem.id, quantity = it.quantity) }
            val request = PlaceOrderRequest(
                restaurantId = restaurantId,
                items = itemsDto,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                couponCode = couponCode
            )
            val response = ApiClient.api.placeFoodOrder(request)
            if (response.isSuccessful && response.body()?.order != null) {
                ApiResult.Success(response.body()!!.order!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Order failed (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun getFoodOrderStatus(orderId: String): ApiResult<FoodOrderDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getFoodOrderStatus(orderId)
            if (response.isSuccessful && response.body()?.order != null) {
                ApiResult.Success(response.body()!!.order!!)
            } else {
                ApiResult.Error("Order not found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateFoodOrderStatus(orderId: String, status: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.updateFoodOrderStatus(orderId, UpdateOrderStatusRequest(status))
            if (response.isSuccessful) ApiResult.Success(true) else ApiResult.Error("Update failed")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun reorderFood(orderId: String): ApiResult<FoodOrderDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.reorderFood(orderId)
            if (response.isSuccessful && response.body()?.order != null) {
                ApiResult.Success(response.body()!!.order!!)
            } else {
                ApiResult.Error("Reorder failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // --- Parcel Delivery ---

    suspend fun estimateParcel(weightKg: Float, distanceKm: Float = 4.5f, category: String = "DOCUMENTS"): ApiResult<ParcelBreakdownDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.estimateParcel(EstimateParcelRequest(weightKg, distanceKm, category))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val breakdown = body.breakdown ?: ParcelBreakdownDto(50.0, 0.0, 30.0, body.estimatedPrice)
                ApiResult.Success(breakdown)
            } else {
                ApiResult.Error("Estimation failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun bookParcel(
        recipientName: String,
        recipientPhone: String,
        recipientAddress: String,
        weightKg: Float,
        category: String,
        instructions: String? = null,
        senderName: String? = null,
        senderPhone: String? = null,
        pickupAddress: String? = null,
        paymentMethod: String = "Online"
    ): ApiResult<ParcelDetailDto> = withContext(Dispatchers.IO) {
        try {
            val request = BookParcelRequest(
                recipientName = recipientName,
                recipientPhone = recipientPhone,
                recipientAddress = recipientAddress,
                weightKg = weightKg,
                category = category,
                instructions = instructions,
                senderName = senderName,
                senderPhone = senderPhone,
                pickupAddress = pickupAddress,
                paymentMethod = paymentMethod
            )
            val response = ApiClient.api.bookParcel(request)
            if (response.isSuccessful && response.body()?.parcel != null) {
                ApiResult.Success(response.body()!!.parcel!!)
            } else {
                ApiResult.Error(response.errorBody()?.string() ?: "Parcel booking failed")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getParcelStatus(trackingId: String): ApiResult<ParcelDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getParcelStatus(trackingId)
            if (response.isSuccessful && response.body()?.parcel != null) {
                ApiResult.Success(response.body()!!.parcel!!)
            } else {
                ApiResult.Error("Parcel not found")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun updateParcelStatus(trackingId: String, status: String): ApiResult<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.updateParcelStatus(trackingId, UpdateOrderStatusRequest(status))
            if (response.isSuccessful) ApiResult.Success(true) else ApiResult.Error("Update failed")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Network error")
        }
    }

    // --- Activities & Locations ---

    suspend fun fetchActivities(serviceType: String? = null): ApiResult<List<ActivityItem>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getActivities(serviceType)
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.activities.map { dto ->
                    val sType = when (dto.serviceType.uppercase()) {
                        "CAB", "GO", "PREMIER", "XL" -> ServiceType.CAB
                        "BIKE", "MOTO" -> ServiceType.BIKE
                        "FOOD" -> ServiceType.FOOD
                        "PARCEL" -> ServiceType.PARCEL
                        else -> ServiceType.CAB
                    }
                    ActivityItem(
                        id = dto.id,
                        title = dto.title,
                        serviceType = sType,
                        dateFormatted = dto.dateFormatted,
                        priceFormatted = dto.priceFormatted,
                        status = dto.status,
                        startLocation = dto.startLocation,
                        endLocation = dto.endLocation,
                        itemsSummary = dto.itemsSummary,
                        imageRes = resolveImageRes(dto.imageKey)
                    )
                }
                return@withContext ApiResult.Success(list)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not fetch activities")
    }

    suspend fun fetchLocations(): ApiResult<List<LocationItem>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getRecentLocations()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.locations.map { dto ->
                    LocationItem(
                        id = dto.id,
                        title = dto.title,
                        address = dto.address,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        type = when (dto.type.uppercase()) {
                            "WORK" -> LocationType.WORK
                            "HOME" -> LocationType.HOME
                            "AIRPORT" -> LocationType.AIRPORT
                            "POPULAR" -> LocationType.POPULAR
                            else -> LocationType.RECENT
                        }
                    )
                }
                return@withContext ApiResult.Success(list)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not fetch locations")
    }

    suspend fun searchLocations(query: String): ApiResult<List<LocationItem>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.searchLocations(query)
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.locations.map { dto ->
                    LocationItem(
                        id = dto.id,
                        title = dto.title,
                        address = dto.address,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        type = when (dto.type.uppercase()) {
                            "WORK" -> LocationType.WORK
                            "HOME" -> LocationType.HOME
                            "AIRPORT" -> LocationType.AIRPORT
                            "POPULAR" -> LocationType.POPULAR
                            else -> LocationType.RECENT
                        }
                    )
                }
                return@withContext ApiResult.Success(list)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not search locations")
    }

    suspend fun getDrivingRoute(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): ApiResult<RouteResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.getRoute(fromLat, fromLng, toLat, toLng)
            if (response.isSuccessful && response.body()?.success == true) {
                return@withContext ApiResult.Success(response.body()!!)
            }
        } catch (e: Exception) {
            // fallback
        }
        ApiResult.Error("Could not fetch route")
    }


    private fun resolveImageRes(key: String?): Int {
        return when (key) {
            "img_chicken_biryani" -> R.drawable.img_chicken_biryani
            "img_mutton_biryani" -> R.drawable.img_mutton_biryani
            "img_paneer_biryani" -> R.drawable.img_paneer_biryani
            "img_galouti_kebab" -> R.drawable.img_galouti_kebab
            "img_butter_naan" -> R.drawable.img_butter_naan
            "img_dal_makhani" -> R.drawable.img_dal_makhani
            "img_paneer_masala" -> R.drawable.img_paneer_masala
            "img_garlic_naan" -> R.drawable.img_garlic_naan
            "img_veg_thali" -> R.drawable.img_veg_thali
            "img_margherita_pizza" -> R.drawable.img_margherita_pizza
            "img_pepperoni_pizza" -> R.drawable.img_pepperoni_pizza
            "img_truffle_pasta" -> R.drawable.img_truffle_pasta
            "img_garlic_bread" -> R.drawable.img_garlic_bread
            "img_masala_dosa" -> R.drawable.img_masala_dosa
            "img_idli_vada" -> R.drawable.img_idli_vada
            "img_filter_coffee" -> R.drawable.img_filter_coffee
            "img_biryani" -> R.drawable.img_biryani
            "img_north_indian" -> R.drawable.img_north_indian
            "img_pizza" -> R.drawable.img_pizza
            "img_south_indian" -> R.drawable.img_south_indian
            "img_cab_go" -> R.drawable.img_cab_go
            "img_cab_premier" -> R.drawable.img_cab_premier
            "img_cab_xl" -> R.drawable.img_cab_xl
            "img_auto" -> R.drawable.img_auto
            "img_moto" -> R.drawable.img_moto
            "img_service_parcel" -> R.drawable.img_service_parcel
            else -> R.drawable.img_cab_go
        }
    }
}

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}
