package com.yugo.app.data.api

import android.content.Context
import android.os.Build
import com.yugo.app.data.local.SessionManager
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface YugoApi {

    @GET("health")
    suspend fun getHealth(): retrofit2.Response<HealthResponse>

    // --- Authentication & Account ---

    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: RequestOtpRequest): retrofit2.Response<RequestOtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): retrofit2.Response<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): retrofit2.Response<RefreshTokenResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): retrofit2.Response<SimpleSuccessResponse>

    @GET("auth/profile")
    suspend fun getProfile(): retrofit2.Response<AuthResponseDto>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): retrofit2.Response<AuthResponseDto>

    @POST("auth/addresses")
    suspend fun addAddress(@Body request: CreateAddressRequest): retrofit2.Response<Map<String, Any>>

    @DELETE("auth/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): retrofit2.Response<SimpleSuccessResponse>

    @POST("auth/emergency-contacts")
    suspend fun addEmergencyContact(@Body request: CreateEmergencyContactRequest): retrofit2.Response<Map<String, Any>>

    @DELETE("auth/emergency-contacts/{id}")
    suspend fun deleteEmergencyContact(@Path("id") id: String): retrofit2.Response<SimpleSuccessResponse>

    @PUT("auth/preferences")
    suspend fun updatePreferences(@Body request: NotificationPreferencesDto): retrofit2.Response<Map<String, Any>>

    // --- Rides ---

    @GET("rides/options")
    suspend fun getRideOptions(
        @Query("distanceKm") distanceKm: Double = 5.8
    ): retrofit2.Response<RideOptionsResponse>

    @GET("rides/drivers/nearby")
    suspend fun getNearbyDrivers(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): retrofit2.Response<DriversResponse>

    @POST("rides/book")
    suspend fun bookRide(
        @Body request: CreateRideRequest
    ): retrofit2.Response<BookingResponseDto>

    @GET("rides/{id}")
    suspend fun getRideTracking(
        @Path("id") id: String
    ): retrofit2.Response<BookingResponseDto>

    @POST("rides/{id}/status")
    suspend fun updateRideStatus(
        @Path("id") id: String,
        @Body request: UpdateRideStatusRequest
    ): retrofit2.Response<SimpleSuccessResponse>

    @POST("rides/{id}/cancel")
    suspend fun cancelRide(
        @Path("id") id: String
    ): retrofit2.Response<SimpleSuccessResponse>

    // --- Food Delivery ---

    @GET("food/restaurants")
    suspend fun getRestaurants(
        @Query("cuisine") cuisine: String? = null,
        @Query("search") search: String? = null
    ): retrofit2.Response<RestaurantsResponse>

    @GET("food/restaurants/{id}")
    suspend fun getRestaurantDetails(
        @Path("id") id: String
    ): retrofit2.Response<RestaurantDto>

    @POST("food/calculate-bill")
    suspend fun calculateFoodBill(
        @Body request: CalculateBillRequest
    ): retrofit2.Response<CalculateBillResponse>

    @POST("food/orders")
    suspend fun placeFoodOrder(
        @Body request: PlaceOrderRequest
    ): retrofit2.Response<FoodOrderResponseDto>

    @GET("food/orders/{id}")
    suspend fun getFoodOrderStatus(
        @Path("id") id: String
    ): retrofit2.Response<FoodOrderResponseDto>

    @POST("food/orders/{id}/status")
    suspend fun updateFoodOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): retrofit2.Response<SimpleSuccessResponse>

    @POST("food/orders/{id}/reorder")
    suspend fun reorderFood(
        @Path("id") id: String
    ): retrofit2.Response<FoodOrderResponseDto>

    // --- Parcel Delivery ---

    @POST("parcel/estimate")
    suspend fun estimateParcel(
        @Body request: EstimateParcelRequest
    ): retrofit2.Response<EstimateParcelResponse>

    @POST("parcel/book")
    suspend fun bookParcel(
        @Body request: BookParcelRequest
    ): retrofit2.Response<BookParcelResponseDto>

    @GET("parcel/{trackingId}")
    suspend fun getParcelStatus(
        @Path("trackingId") trackingId: String
    ): retrofit2.Response<BookParcelResponseDto>

    @POST("parcel/{trackingId}/status")
    suspend fun updateParcelStatus(
        @Path("trackingId") trackingId: String,
        @Body request: UpdateOrderStatusRequest
    ): retrofit2.Response<SimpleSuccessResponse>

    // --- Wallet & Payments ---

    @POST("payments/wallet/topup")
    suspend fun topupWallet(
        @Body request: TopupWalletRequest
    ): retrofit2.Response<TopupWalletResponse>

    // --- Activities & Locations ---

    @GET("activities")
    suspend fun getActivities(
        @Query("serviceType") serviceType: String? = null
    ): retrofit2.Response<ActivitiesResponse>

    @GET("locations/recent")
    suspend fun getRecentLocations(): retrofit2.Response<LocationsResponse>

    @GET("locations/search")
    suspend fun searchLocations(
        @Query("q") query: String
    ): retrofit2.Response<LocationsResponse>

    @GET("locations/route")
    suspend fun getRoute(
        @Query("fromLat") fromLat: Double,
        @Query("fromLng") fromLng: Double,
        @Query("toLat") toLat: Double,
        @Query("toLng") toLng: Double
    ): retrofit2.Response<RouteResponseDto>
}

object ApiClient {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    fun getBaseUrl(): String {
        appContext?.let { ctx ->
            val custom = SessionManager.getInstance(ctx).getCustomBaseUrl()
            if (!custom.isNullOrBlank()) {
                return if (custom.endsWith("/")) custom else "$custom/"
            }
        }
        return "https://yugo-backend.onrender.com/api/v1/"
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = appContext?.let { SessionManager.getInstance(it).getAccessToken() }
        val builder = original.newBuilder()
            .header("Bypass-Tunnel-Reminder", "true")
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        chain.proceed(builder.build())
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            val ctx = appContext ?: return null
            val sessionManager = SessionManager.getInstance(ctx)
            val refreshToken = sessionManager.getRefreshToken() ?: return null

            // Avoid infinite loops if refreshing already failed
            if (response.request.header("Authorization-Retry") != null) {
                sessionManager.clearSession()
                return null
            }

            // Attempt synchronous refresh
            try {
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = "{\"refreshToken\":\"$refreshToken\"}".toRequestBody(mediaType)
                val refreshRequest = Request.Builder()
                    .url(getBaseUrl() + "auth/refresh")
                    .post(requestBody)
                    .build()

                val refreshClient = OkHttpClient.Builder().build()
                val refreshResponse = refreshClient.newCall(refreshRequest).execute()

                val responseBody = refreshResponse.body
                if (refreshResponse.isSuccessful && responseBody != null) {
                    val bodyString = responseBody.string()
                    val gson = com.google.gson.Gson()
                    val result = gson.fromJson(bodyString, RefreshTokenResponse::class.java)
                    if (result.success && !result.accessToken.isNullOrBlank()) {
                        sessionManager.setAccessToken(result.accessToken)
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${result.accessToken}")
                            .header("Authorization-Retry", "true")
                            .build()
                    }
                }
            } catch (e: Exception) {
                // Refresh failed
            }

            sessionManager.clearSession()
            return null
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    val api: YugoApi by lazy {
        Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YugoApi::class.java)
    }
}
