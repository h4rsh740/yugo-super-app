package com.yugo.app.ui.screens.parcel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.api.ParcelBreakdownDto
import com.yugo.app.data.api.ParcelDetailDto
import com.yugo.app.data.model.ParcelCategory
import com.yugo.app.data.model.ParcelStatus
import com.yugo.app.data.repository.YugoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParcelUiState(
    val pickupAddress: String = "DLF Phase 2, Gurugram",
    val senderName: String = "Harsh Singh",
    val senderPhone: String = "+91 98765 00112",
    val recipientAddress: String = "Cyber City Phase 3, Gurugram",
    val recipientName: String = "Aman Verma",
    val recipientPhone: String = "+91 98123 45678",
    val selectedCategory: ParcelCategory = ParcelCategory.DOCUMENTS,
    val weightKg: Float = 1.0f,
    val instructions: String = "Handle with care",
    val parcelStatus: ParcelStatus = ParcelStatus.CREATED,
    val trackingId: String? = null,
    val deliveryPin: String = "7921",
    val courierName: String = "Vikram Singh",
    val courierPhone: String = "+91 98765 88990",
    val priceBreakdown: ParcelBreakdownDto? = null,
    val isBooking: Boolean = false,
    val isDelivered: Boolean = false,
    val errorMessage: String? = null
) {
    val estimatedPrice: Double
        get() = priceBreakdown?.total ?: (50.0 + (weightKg.coerceAtLeast(1.0f) - 1.0) * 20.0 + 48.0)
}

class ParcelViewModel(
    private val repository: YugoRepository = YugoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParcelUiState())
    val uiState: StateFlow<ParcelUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    init {
        fetchPriceEstimate()
    }

    fun updatePickup(address: String) {
        _uiState.value = _uiState.value.copy(pickupAddress = address)
        fetchPriceEstimate()
    }

    fun updateRecipient(name: String, phone: String, address: String) {
        _uiState.value = _uiState.value.copy(
            recipientName = name,
            recipientPhone = phone,
            recipientAddress = address
        )
        fetchPriceEstimate()
    }

    fun selectCategory(category: ParcelCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        fetchPriceEstimate()
    }

    fun setWeight(weight: Float) {
        val safeWeight = weight.coerceIn(0.1f, 20.0f)
        _uiState.value = _uiState.value.copy(weightKg = safeWeight)
        fetchPriceEstimate()
    }

    fun setInstructions(notes: String) {
        _uiState.value = _uiState.value.copy(instructions = notes)
    }

    private fun fetchPriceEstimate() {
        viewModelScope.launch {
            val result = repository.estimateParcel(
                weightKg = _uiState.value.weightKg,
                distanceKm = 4.8f,
                category = _uiState.value.selectedCategory.name
            )
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(priceBreakdown = result.data)
            }
        }
    }

    fun confirmParcelBooking(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        if (state.recipientName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Recipient name is required")
            return
        }
        if (state.recipientPhone.filter { it.isDigit() }.length < 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Valid 10-digit recipient phone number is required")
            return
        }
        if (state.recipientAddress.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Recipient delivery address is required")
            return
        }

        _uiState.value = _uiState.value.copy(isBooking = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.bookParcelOnServer(
                recipientName = state.recipientName,
                recipientPhone = state.recipientPhone,
                recipientAddress = state.recipientAddress,
                weightKg = state.weightKg,
                category = state.selectedCategory.name,
                instructions = state.instructions,
                senderName = state.senderName,
                senderPhone = state.senderPhone,
                pickupAddress = state.pickupAddress,
                paymentMethod = "Online UPI"
            )

            if (result is ApiResult.Success) {
                val parcel = result.data
                _uiState.value = _uiState.value.copy(
                    isBooking = false,
                    trackingId = parcel.trackingId,
                    deliveryPin = parcel.deliveryPin ?: "7921",
                    courierName = parcel.riderName ?: "Vikram Singh",
                    courierPhone = parcel.riderPhone ?: "+91 98765 88990",
                    parcelStatus = ParcelStatus.COURIER_ASSIGNED
                )
                onSuccess()
                startTrackingLifecycle(parcel.trackingId)
            } else if (result is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isBooking = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun startTrackingLifecycle(trackingId: String) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            // 1. RIDER_ASSIGNED
            delay(3500)
            repository.updateParcelStatus(trackingId, "PICKED_UP")
            _uiState.value = _uiState.value.copy(parcelStatus = ParcelStatus.PICKED_UP)

            // 2. IN_TRANSIT
            delay(4000)
            repository.updateParcelStatus(trackingId, "IN_TRANSIT")
            _uiState.value = _uiState.value.copy(parcelStatus = ParcelStatus.IN_TRANSIT)

            // 3. DELIVERED
            delay(5000)
            repository.updateParcelStatus(trackingId, "DELIVERED")
            _uiState.value = _uiState.value.copy(
                parcelStatus = ParcelStatus.DELIVERED,
                isDelivered = true
            )
        }
    }

    fun resetParcel() {
        trackingJob?.cancel()
        _uiState.value = ParcelUiState()
        fetchPriceEstimate()
    }
}
