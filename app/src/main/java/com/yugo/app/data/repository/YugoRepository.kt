package com.yugo.app.data.repository

import com.yugo.app.R
import com.yugo.app.data.api.*
import com.yugo.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YugoRepository(
    val apiService: YugoApiService = YugoApiService()
) {

    val defaultLocations = listOf(
        LocationItem("1", "Cyber City", "DLF Phase 2, Gurugram, Haryana", 28.4950, 77.0895, LocationType.WORK),
        LocationItem("2", "Home", "Sector 54, Golf Course Road, Gurugram", 28.4382, 77.1082, LocationType.HOME),
        LocationItem("3", "IGI Airport T3", "Indira Gandhi International Airport, New Delhi", 28.5562, 77.1000, LocationType.AIRPORT),
        LocationItem("4", "Connaught Place", "Inner Circle, New Delhi", 28.6315, 77.2167, LocationType.POPULAR),
        LocationItem("5", "Galleria Market", "DLF Phase 4, Gurugram, Haryana", 28.4674, 77.0825, LocationType.RECENT)
    )

    val recentLocations: List<LocationItem> get() = defaultLocations

    val sampleRestaurants: List<Restaurant> = listOf(
        Restaurant(
            id = "rest_1",
            name = "Bikanervala",
            cuisine = "North Indian, Street Food, Mithai",
            rating = 4.4,
            ratingCount = "5.2k",
            deliveryTimeMinutes = 28,
            priceForTwo = 400,
            distanceKm = 2.1,
            isPureVeg = true,
            offerText = "50% OFF up to ₹100",
            imageRes = R.drawable.img_north_indian,
            menuItems = listOf(
                MenuItem("m1", "Special Dal Makhani", "Slow-cooked black lentils in butter & rich cream", 260.0, true, "Mains", true, imageRes = R.drawable.img_dal_makhani),
                MenuItem("m2", "Paneer Butter Masala", "Cottage cheese cubes in spiced buttery tomato gravy", 290.0, true, "Mains", true, imageRes = R.drawable.img_paneer_masala),
                MenuItem("m3", "Butter Naan", "Tandoori flatbread brushed with fresh butter", 55.0, true, "Breads", false, imageRes = R.drawable.img_butter_naan),
                MenuItem("m4", "Garlic Naan", "Tandoori flatbread topped with minced garlic and herbs", 70.0, true, "Breads", true, imageRes = R.drawable.img_garlic_naan),
                MenuItem("m5", "Royal Veg Thali", "Dal, paneer, dry subji, 2 rotis, rice, raita, gulab jamun", 360.0, true, "Thalis", true, imageRes = R.drawable.img_veg_thali)
            )
        ),
        Restaurant(
            id = "rest_2",
            name = "Behrouz Biryani",
            cuisine = "Royal Awadhi Biryani, Mughlai",
            rating = 4.6,
            ratingCount = "3.8k",
            deliveryTimeMinutes = 35,
            priceForTwo = 700,
            distanceKm = 3.4,
            isPureVeg = false,
            offerText = "Flat ₹125 OFF on ₹499+",
            imageRes = R.drawable.img_biryani,
            menuItems = listOf(
                MenuItem("m6", "Dum Gosht Biryani", "Succulent mutton pieces marinated in royal spices layered with long-grain rice", 460.0, false, "Biryani", true, imageRes = R.drawable.img_mutton_biryani),
                MenuItem("m7", "Murgh Makhani Biryani", "Boneless chicken infused with velvety spiced saffron rice", 380.0, false, "Biryani", true, imageRes = R.drawable.img_chicken_biryani),
                MenuItem("m8", "Subz-e-Biryani", "Fresh paneer and seasonal garden vegetables in scented basmati", 310.0, true, "Biryani", false, imageRes = R.drawable.img_paneer_biryani),
                MenuItem("m9", "Galouti Kebab (4 pcs)", "Melt-in-mouth spiced minced kebabs served with mint chutney", 340.0, false, "Starters", true, imageRes = R.drawable.img_galouti_kebab)
            )
        ),
        Restaurant(
            id = "rest_3",
            name = "Toscano Artisanal Pizzeria",
            cuisine = "Italian, Wood-Fired Pizza, Pasta",
            rating = 4.7,
            ratingCount = "1.9k",
            deliveryTimeMinutes = 30,
            priceForTwo = 850,
            distanceKm = 4.2,
            isPureVeg = false,
            offerText = "Free Garlic Bread on ₹599+",
            imageRes = R.drawable.img_pizza,
            menuItems = listOf(
                MenuItem("m10", "Margherita D.O.P.", "San Marzano tomatoes, fresh buffalo mozzarella, fresh basil", 420.0, true, "Pizza", true, imageRes = R.drawable.img_margherita_pizza),
                MenuItem("m11", "Spicy Pepperoni Rustica", "Slow-cured pork pepperoni, mozzarella, chili flakes", 540.0, false, "Pizza", true, imageRes = R.drawable.img_pepperoni_pizza),
                MenuItem("m12", "Wild Mushroom Truffle Pasta", "Fettuccine tossed in parmesan cream and black truffle oil", 480.0, true, "Pasta", true, imageRes = R.drawable.img_truffle_pasta),
                MenuItem("m13", "Herbed Garlic Bread", "Toasted baguette slices with garlic herb butter", 180.0, true, "Sides", false, imageRes = R.drawable.img_garlic_bread)
            )
        ),
        Restaurant(
            id = "rest_4",
            name = "Sagar Ratna",
            cuisine = "South Indian, Dosas, Filter Coffee",
            rating = 4.5,
            ratingCount = "4.1k",
            deliveryTimeMinutes = 22,
            priceForTwo = 350,
            distanceKm = 1.8,
            isPureVeg = true,
            offerText = "20% OFF up to ₹50",
            imageRes = R.drawable.img_south_indian,
            menuItems = listOf(
                MenuItem("m14", "Mysore Masala Dosa", "Crispy golden crepe with spicy red chutney and potato mash", 195.0, true, "Dosas", true, imageRes = R.drawable.img_masala_dosa),
                MenuItem("m15", "Steamed Idli & Medu Vada Combo", "Two soft idlis and one crispy vada with sambar and 3 chutneys", 150.0, true, "Tiffin", true, imageRes = R.drawable.img_idli_vada),
                MenuItem("m16", "Traditional Filter Coffee", "Aromatic chicory blend frothed with hot milk", 75.0, true, "Beverages", true, imageRes = R.drawable.img_filter_coffee)
            )
        )
    )

    val sampleActivities: List<ActivityItem> = listOf(
        ActivityItem(
            id = "act_1",
            title = "YUGO Go to IGI Airport T3",
            serviceType = ServiceType.CAB,
            dateFormatted = "Yesterday, 6:45 PM",
            priceFormatted = "₹480.00",
            status = "Completed",
            startLocation = "DLF Cyber City, Tower B",
            endLocation = "IGI Airport Terminal 3",
            imageRes = R.drawable.img_cab_go
        ),
        ActivityItem(
            id = "act_2",
            title = "Dinner from Behrouz Biryani",
            serviceType = ServiceType.FOOD,
            dateFormatted = "14 Oct, 9:15 PM",
            priceFormatted = "₹590.00",
            status = "Delivered",
            startLocation = "Behrouz Biryani, Sector 29",
            endLocation = "Sector 54, Golf Course Road",
            itemsSummary = "1x Dum Gosht Biryani, 1x Galouti Kebab",
            imageRes = R.drawable.img_biryani
        ),
        ActivityItem(
            id = "act_3",
            title = "Document Courier to Connaught Place",
            serviceType = ServiceType.PARCEL,
            dateFormatted = "12 Oct, 11:30 AM",
            priceFormatted = "₹135.00",
            status = "Delivered",
            startLocation = "Galleria Market, DLF Phase 4",
            endLocation = "Inner Circle, Connaught Place",
            itemsSummary = "Legal Contract documents (0.5 kg)",
            imageRes = R.drawable.img_parcel_banner
        ),
        ActivityItem(
            id = "act_4",
            title = "YUGO Auto to Galleria Market",
            serviceType = ServiceType.BIKE,
            dateFormatted = "10 Oct, 3:10 PM",
            priceFormatted = "₹65.00",
            status = "Completed",
            startLocation = "Sector 54, Golf Course Road",
            endLocation = "Galleria Market",
            imageRes = R.drawable.img_auto
        )
    )

    private val _cachedLocations = MutableStateFlow(defaultLocations)
    val cachedLocations: StateFlow<List<LocationItem>> = _cachedLocations.asStateFlow()

    private val _cachedRestaurants = MutableStateFlow(sampleRestaurants)
    val cachedRestaurants: StateFlow<List<Restaurant>> = _cachedRestaurants.asStateFlow()

    private val _cachedActivities = MutableStateFlow(sampleActivities)
    val cachedActivities: StateFlow<List<ActivityItem>> = _cachedActivities.asStateFlow()

    // --- Authentication ---

    suspend fun requestOtp(phone: String): ApiResult<RequestOtpResponse> {
        return apiService.requestOtp(phone)
    }

    suspend fun verifyOtp(phone: String, otp: String): ApiResult<AuthResponseDto> {
        return apiService.verifyOtp(phone, otp)
    }

    suspend fun fetchProfile(): ApiResult<UserProfileDto> {
        return apiService.fetchProfile()
    }

    suspend fun updateProfile(name: String, email: String): ApiResult<UserProfileDto> {
        return apiService.updateProfile(name, email)
    }

    suspend fun addSavedAddress(title: String, address: String, label: String): ApiResult<Boolean> {
        return apiService.addSavedAddress(title, address, label)
    }

    suspend fun deleteSavedAddress(id: String): ApiResult<Boolean> {
        return apiService.deleteSavedAddress(id)
    }

    suspend fun addEmergencyContact(name: String, phone: String, relationship: String): ApiResult<Boolean> {
        return apiService.addEmergencyContact(name, phone, relationship)
    }

    suspend fun deleteEmergencyContact(id: String): ApiResult<Boolean> {
        return apiService.deleteEmergencyContact(id)
    }

    suspend fun updatePreferences(prefs: NotificationPreferencesDto): ApiResult<Boolean> {
        return apiService.updatePreferences(prefs)
    }

    suspend fun topupWallet(amount: Double, method: String = "UPI"): ApiResult<Double> {
        return apiService.topupWallet(amount, method)
    }

    // --- Rides ---

    fun getDefaultRideOptions(distanceKm: Double = 5.8): List<RideOption> {
        val km = distanceKm.coerceAtLeast(1.0)
        return listOf(
            RideOption(
                id = "opt_auto",
                name = "YUGO Auto",
                category = RideCategory.AUTO,
                capacity = 3,
                etaMinutes = 2,
                fareAmount = Math.max(50.0, 35.0 + km * 12.0),
                description = "Quick auto rides without haggling",
                isRecommended = false,
                imageRes = R.drawable.img_auto
            ),
            RideOption(
                id = "opt_moto",
                name = "YUGO Moto",
                category = RideCategory.MOTO,
                capacity = 1,
                etaMinutes = 1,
                fareAmount = Math.max(35.0, 25.0 + km * 8.0),
                description = "Fastest through traffic",
                isRecommended = false,
                imageRes = R.drawable.img_moto
            ),
            RideOption(
                id = "opt_go",
                name = "YUGO Go",
                category = RideCategory.GO,
                capacity = 4,
                etaMinutes = 3,
                fareAmount = Math.max(90.0, 50.0 + km * 16.0),
                description = "Affordable, comfortable hatchbacks",
                isRecommended = true,
                imageRes = R.drawable.img_cab_go
            ),
            RideOption(
                id = "opt_premier",
                name = "YUGO Premier",
                category = RideCategory.PREMIER,
                capacity = 4,
                etaMinutes = 4,
                fareAmount = Math.max(150.0, 80.0 + km * 22.0),
                description = "Top-rated drivers, premium sedans",
                isRecommended = false,
                imageRes = R.drawable.img_cab_premier
            ),
            RideOption(
                id = "opt_xl",
                name = "YUGO XL",
                category = RideCategory.XL,
                capacity = 6,
                etaMinutes = 5,
                fareAmount = Math.max(220.0, 120.0 + km * 30.0),
                description = "Spacious SUVs for group rides & luggage",
                isRecommended = false,
                imageRes = R.drawable.img_cab_xl
            )
        )
    }

    suspend fun getAvailableRideOptions(distanceKm: Double = 5.8): List<RideOption> {
        val result = apiService.getRideOptions(distanceKm)
        return if (result is ApiResult.Success) {
            result.data
        } else {
            getDefaultRideOptions(distanceKm)
        }
    }

    suspend fun fetchNearbyDrivers(lat: Double, lng: Double): List<DriverInfo> {
        val result = apiService.getNearbyDrivers(lat, lng)
        return if (result is ApiResult.Success) {
            result.data
        } else {
            listOf(
                DriverInfo(
                    id = "drv_1092",
                    name = "Rajesh Sharma",
                    rating = 4.92,
                    totalTrips = 3840,
                    vehicleModel = "Maruti Suzuki Dzire (White)",
                    vehicleColor = "White",
                    plateNumber = "HR 26 DQ 4921",
                    phoneNumber = "+91 98765 43210",
                    otpPin = "4819",
                    latitude = lat + 0.002,
                    longitude = lng + 0.003
                )
            )
        }
    }

    suspend fun bookRideOnServer(
        pickup: LocationItem,
        destination: LocationItem,
        rideOption: RideOption,
        paymentMethod: String = "Cash"
    ): ApiResult<BookingDetailDto> {
        return apiService.createRideBooking(pickup, destination, rideOption, paymentMethod)
    }

    suspend fun getRideTracking(bookingId: String): ApiResult<BookingDetailDto> {
        return apiService.getRideTracking(bookingId)
    }

    suspend fun updateRideStatus(bookingId: String, status: String): ApiResult<Boolean> {
        return apiService.updateRideStatus(bookingId, status)
    }

    suspend fun cancelRide(bookingId: String): ApiResult<Boolean> {
        return apiService.cancelRide(bookingId)
    }

    // --- Food Delivery ---

    suspend fun fetchLiveRestaurants(cuisine: String? = null, search: String? = null): List<Restaurant> {
        val result = apiService.fetchRestaurants(cuisine, search)
        return if (result is ApiResult.Success) {
            _cachedRestaurants.value = result.data
            result.data
        } else {
            _cachedRestaurants.value
        }
    }

    suspend fun calculateFoodBill(restaurantId: String, cartItems: List<CartItem>, couponCode: String? = null): ApiResult<BillBreakdownDto> {
        return apiService.calculateFoodBill(restaurantId, cartItems, couponCode)
    }

    suspend fun placeFoodOrderOnServer(
        restaurantId: String,
        cartItems: List<CartItem>,
        deliveryAddress: String,
        paymentMethod: String = "Online",
        couponCode: String? = null
    ): ApiResult<FoodOrderDetailDto> {
        return apiService.placeFoodOrder(restaurantId, cartItems, deliveryAddress, paymentMethod, couponCode)
    }

    suspend fun getFoodOrderStatus(orderId: String): ApiResult<FoodOrderDetailDto> {
        return apiService.getFoodOrderStatus(orderId)
    }

    suspend fun updateFoodOrderStatus(orderId: String, status: String): ApiResult<Boolean> {
        return apiService.updateFoodOrderStatus(orderId, status)
    }

    suspend fun reorderFood(orderId: String): ApiResult<FoodOrderDetailDto> {
        return apiService.reorderFood(orderId)
    }

    // --- Parcel Delivery ---

    suspend fun estimateParcel(weightKg: Float, distanceKm: Float = 4.5f, category: String = "DOCUMENTS"): ApiResult<ParcelBreakdownDto> {
        return apiService.estimateParcel(weightKg, distanceKm, category)
    }

    suspend fun bookParcelOnServer(
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
    ): ApiResult<ParcelDetailDto> {
        return apiService.bookParcel(
            recipientName, recipientPhone, recipientAddress, weightKg, category,
            instructions, senderName, senderPhone, pickupAddress, paymentMethod
        )
    }

    suspend fun getParcelStatus(trackingId: String): ApiResult<ParcelDetailDto> {
        return apiService.getParcelStatus(trackingId)
    }

    suspend fun updateParcelStatus(trackingId: String, status: String): ApiResult<Boolean> {
        return apiService.updateParcelStatus(trackingId, status)
    }

    // --- Activities & Locations ---

    suspend fun fetchLiveActivities(serviceType: String? = null): List<ActivityItem> {
        val result = apiService.fetchActivities(serviceType)
        return if (result is ApiResult.Success) {
            _cachedActivities.value = result.data
            result.data
        } else {
            _cachedActivities.value
        }
    }

    suspend fun fetchLocations(): List<LocationItem> {
        val result = apiService.fetchLocations()
        return if (result is ApiResult.Success) {
            _cachedLocations.value = result.data
            result.data
        } else {
            _cachedLocations.value
        }
    }

    suspend fun searchLocations(query: String): List<LocationItem> {
        if (query.isBlank()) return _cachedLocations.value
        val result = apiService.searchLocations(query)
        return if (result is ApiResult.Success && result.data.isNotEmpty()) {
            result.data
        } else {
            _cachedLocations.value.filter {
                it.title.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true)
            }
        }
    }

    suspend fun getDrivingRoute(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): RouteResponseDto? {
        val result = apiService.getDrivingRoute(fromLat, fromLng, toLat, toLng)
        return if (result is ApiResult.Success) result.data else null
    }
}

