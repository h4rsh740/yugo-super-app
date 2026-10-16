package com.yugo.app.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yugo.app.data.api.ApiResult
import com.yugo.app.data.api.BillBreakdownDto
import com.yugo.app.data.api.FoodOrderDetailDto
import com.yugo.app.data.model.CartItem
import com.yugo.app.data.model.MenuItem
import com.yugo.app.data.model.Restaurant
import com.yugo.app.data.repository.YugoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FoodUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val selectedCuisine: String? = null,
    val selectedRestaurant: Restaurant? = null,
    val cartItems: List<CartItem> = emptyList(),
    val searchQuery: String = "",
    val billBreakdown: BillBreakdownDto? = null,
    val deliveryAddress: String = "Sector 54, Golf Course Road, Gurugram",
    val paymentMethod: String = "Online UPI",
    val couponCode: String? = null,
    val isPlacingOrder: Boolean = false,
    val activeOrder: FoodOrderDetailDto? = null,
    val activeOrderStatus: String? = null,
    val isOrderDelivered: Boolean = false,
    val errorMessage: String? = null
) {
    val cartTotalAmount: Double
        get() = billBreakdown?.totalAmount ?: (cartItems.sumOf { it.menuItem.price * it.quantity } + 50.0)

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }
}

class FoodViewModel(
    private val repository: YugoRepository = YugoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    private var orderLifecycleJob: Job? = null

    init {
        loadRestaurants()
    }

    fun loadRestaurants(cuisine: String? = null, search: String? = null) {
        viewModelScope.launch {
            val list = repository.fetchLiveRestaurants(cuisine, search)
            _uiState.value = _uiState.value.copy(
                restaurants = list,
                selectedCuisine = cuisine,
                searchQuery = search ?: ""
            )
        }
    }

    fun selectCuisine(cuisine: String?) {
        val next = if (_uiState.value.selectedCuisine == cuisine) null else cuisine
        loadRestaurants(cuisine = next, search = _uiState.value.searchQuery.ifBlank { null })
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadRestaurants(cuisine = _uiState.value.selectedCuisine, search = query.ifBlank { null })
    }

    fun selectRestaurant(restaurant: Restaurant) {
        _uiState.value = _uiState.value.copy(selectedRestaurant = restaurant)
    }

    fun addToCart(item: MenuItem) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == item.id }
        if (index != -1) {
            val existing = currentCart[index]
            currentCart[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentCart.add(CartItem(menuItem = item, quantity = 1))
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
        recalculateBill()
    }

    fun removeFromCart(item: MenuItem) {
        val currentCart = _uiState.value.cartItems.toMutableList()
        val index = currentCart.indexOfFirst { it.menuItem.id == item.id }
        if (index != -1) {
            val existing = currentCart[index]
            if (existing.quantity > 1) {
                currentCart[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                currentCart.removeAt(index)
            }
        }
        _uiState.value = _uiState.value.copy(cartItems = currentCart)
        recalculateBill()
    }

    fun setDeliveryAddress(address: String) {
        _uiState.value = _uiState.value.copy(deliveryAddress = address)
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun applyCoupon(code: String) {
        _uiState.value = _uiState.value.copy(couponCode = code)
        recalculateBill()
    }

    private fun recalculateBill() {
        val rest = _uiState.value.selectedRestaurant ?: return
        val cart = _uiState.value.cartItems
        if (cart.isEmpty()) {
            _uiState.value = _uiState.value.copy(billBreakdown = null)
            return
        }

        viewModelScope.launch {
            val result = repository.calculateFoodBill(rest.id, cart, _uiState.value.couponCode)
            if (result is ApiResult.Success) {
                _uiState.value = _uiState.value.copy(billBreakdown = result.data)
            }
        }
    }

    fun placeOrder(onSuccess: () -> Unit = {}) {
        val rest = _uiState.value.selectedRestaurant
        val cart = _uiState.value.cartItems
        val address = _uiState.value.deliveryAddress

        if (rest == null || cart.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Cart cannot be empty")
            return
        }
        if (address.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter delivery address")
            return
        }

        _uiState.value = _uiState.value.copy(isPlacingOrder = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.placeFoodOrderOnServer(
                restaurantId = rest.id,
                cartItems = cart,
                deliveryAddress = address,
                paymentMethod = _uiState.value.paymentMethod,
                couponCode = _uiState.value.couponCode
            )

            if (result is ApiResult.Success) {
                val order = result.data
                _uiState.value = _uiState.value.copy(
                    isPlacingOrder = false,
                    activeOrder = order,
                    activeOrderStatus = order.status,
                    cartItems = emptyList(),
                    billBreakdown = null
                )
                onSuccess()
                startOrderLifecycle(order.orderId)
            } else if (result is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(
                    isPlacingOrder = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun startOrderLifecycle(orderId: String) {
        orderLifecycleJob?.cancel()
        orderLifecycleJob = viewModelScope.launch {
            // CONFIRMED
            delay(3000)
            repository.updateFoodOrderStatus(orderId, "PREPARING")
            _uiState.value = _uiState.value.copy(activeOrderStatus = "PREPARING")

            delay(4000)
            repository.updateFoodOrderStatus(orderId, "PICKED_UP")
            _uiState.value = _uiState.value.copy(activeOrderStatus = "PICKED_UP")

            delay(4000)
            repository.updateFoodOrderStatus(orderId, "ARRIVING")
            _uiState.value = _uiState.value.copy(activeOrderStatus = "ARRIVING")

            delay(4000)
            repository.updateFoodOrderStatus(orderId, "DELIVERED")
            _uiState.value = _uiState.value.copy(
                activeOrderStatus = "DELIVERED",
                isOrderDelivered = true
            )
        }
    }

    fun reorder(orderId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.reorderFood(orderId)
            if (result is ApiResult.Success) {
                val order = result.data
                _uiState.value = _uiState.value.copy(
                    activeOrder = order,
                    activeOrderStatus = order.status
                )
                onSuccess()
                startOrderLifecycle(order.orderId)
            }
        }
    }

    fun clearCart() {
        _uiState.value = _uiState.value.copy(cartItems = emptyList(), billBreakdown = null)
    }

    fun resetOrder() {
        orderLifecycleJob?.cancel()
        _uiState.value = _uiState.value.copy(
            activeOrder = null,
            activeOrderStatus = null,
            isOrderDelivered = false
        )
    }
}
