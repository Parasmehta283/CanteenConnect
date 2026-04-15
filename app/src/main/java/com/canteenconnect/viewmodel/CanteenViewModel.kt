package com.canteenconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canteenconnect.data.model.*
import com.canteenconnect.data.repository.CanteenRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CanteenViewModel : ViewModel() {

    private val repo = CanteenRepository()

    // ─── Auth State ───────────────────────────────────────────────────────────
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isAuthChecked = MutableStateFlow(false)
    val isAuthChecked: StateFlow<Boolean> = _isAuthChecked

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    init {
        viewModelScope.launch {
            try {
                _currentUser.value = repo.getCurrentUser()
            } catch (e: Exception) {
                _currentUser.value = null
            } finally {
                _isAuthChecked.value = true
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repo.login(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
            }.onFailure { e ->
                val errorMessage = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Network error: Please check your internet connection and Firebase configuration"
                    e.message?.contains("user not found", ignoreCase = true) == true ->
                        "User not found. Please check your email or sign up first"
                    e.message?.contains("wrong password", ignoreCase = true) == true ->
                        "Incorrect password. Please try again"
                    else -> "Login failed: ${e.message}"
                }
                _authError.value = errorMessage
            }
            _authLoading.value = false
        }
    }

    fun logout() {
        repo.logout()
        _currentUser.value = null
        clearCart()
    }

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repo.signup(name, email, password)
            result.onSuccess { user ->
                _currentUser.value = user
            }.onFailure { e ->
                _authError.value = e.message ?: "Signup failed"
            }
            _authLoading.value = false
        }
    }

    fun clearAuthError() {
        _authError.value = null
        _resetPasswordMessage.value = null
        _resetPasswordSuccess.value = false
    }

    // ─── Password Reset ───────────────────────────────────────────────────────
    private val _resetPasswordLoading = MutableStateFlow(false)
    val resetPasswordLoading: StateFlow<Boolean> = _resetPasswordLoading

    private val _resetPasswordMessage = MutableStateFlow<String?>(null)
    val resetPasswordMessage: StateFlow<String?> = _resetPasswordMessage

    private val _resetPasswordSuccess = MutableStateFlow(false)
    val resetPasswordSuccess: StateFlow<Boolean> = _resetPasswordSuccess

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _resetPasswordLoading.value = true
            _resetPasswordMessage.value = null
            _resetPasswordSuccess.value = false
            try {
                repo.sendPasswordResetEmail(email)
                _resetPasswordMessage.value = "We've sent a password reset link to your email. Please check your inbox."
                _resetPasswordSuccess.value = true
            } catch (e: Exception) {
                _resetPasswordMessage.value = e.message ?: "Failed to send reset email"
                _resetPasswordSuccess.value = false
            } finally {
                _resetPasswordLoading.value = false
            }
        }
    }

    // ─── Menu ─────────────────────────────────────────────────────────────────
    val menuItems: StateFlow<List<MenuItem>> = repo.observeMenuItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addMenuItem(item: MenuItem) {
        viewModelScope.launch { repo.addMenuItem(item) }
    }

    fun updateMenuItem(item: MenuItem) {
        viewModelScope.launch { repo.updateMenuItem(item) }
    }

    fun deleteMenuItem(itemId: String) {
        viewModelScope.launch { repo.deleteMenuItem(itemId) }
    }

    // ─── Image Upload ─────────────────────────────────────────────────────────
    private val _imageUploadLoading = MutableStateFlow(false)
    val imageUploadLoading: StateFlow<Boolean> = _imageUploadLoading

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl

    fun uploadMenuImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _imageUploadLoading.value = true
            try {
                val url = repo.uploadMenuItemImage(imageBytes)
                _uploadedImageUrl.value = url
            } catch (e: Exception) {
                _uploadedImageUrl.value = null
            }
            _imageUploadLoading.value = false
        }
    }

    fun clearUploadedImageUrl() {
        _uploadedImageUrl.value = null
    }

    // ─── Cart ─────────────────────────────────────────────────────────────────
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun addToCart(menuItem: MenuItem) {
        // Don't add unavailable items to cart
        if (!menuItem.available) return
        
        val current = _cartItems.value.toMutableList()
        val existing = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existing >= 0) {
            current[existing] = current[existing].copy(quantity = current[existing].quantity + 1)
        } else {
            current.add(CartItem(menuItem))
        }
        _cartItems.value = current
    }

    fun removeFromCart(menuItem: MenuItem) {
        val current = _cartItems.value.toMutableList()
        val existing = current.indexOfFirst { it.menuItem.id == menuItem.id }
        if (existing >= 0) {
            if (current[existing].quantity > 1) {
                current[existing] = current[existing].copy(quantity = current[existing].quantity - 1)
            } else {
                current.removeAt(existing)
            }
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // ─── Orders ───────────────────────────────────────────────────────────────
    private val _placedOrder = MutableStateFlow<Order?>(null)
    val placedOrder: StateFlow<Order?> = _placedOrder

    private val _orderLoading = MutableStateFlow(false)
    val orderLoading: StateFlow<Boolean> = _orderLoading

    private val _orderError = MutableStateFlow<String?>(null)
    val orderError: StateFlow<String?> = _orderError

    fun placeOrder() {
        val user = _currentUser.value ?: return
        val cart = _cartItems.value
        if (cart.isEmpty()) return
        viewModelScope.launch {
            _orderLoading.value = true
            val result = repo.placeOrder(user, cart)
            result.onSuccess { order ->
                _placedOrder.value = order
                clearCart()
            }.onFailure { e ->
                _orderError.value = e.message ?: "Order failed"
            }
            _orderLoading.value = false
        }
    }

    fun clearPlacedOrder() { _placedOrder.value = null }

    val studentOrders: StateFlow<List<Order>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repo.observeStudentOrders(user.uid) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ─── Staff: Active Orders ─────────────────────────────────────────────────
    val activeOrders: StateFlow<List<Order>> = repo.observeActiveOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateOrderStatus(order: Order) {
        val currentStatus = try { OrderStatus.valueOf(order.status) } catch (e: Exception) { OrderStatus.PLACED }
        val nextStatus = when (currentStatus) {
            OrderStatus.PLACED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY
            OrderStatus.READY -> OrderStatus.COMPLETED
            OrderStatus.COMPLETED -> OrderStatus.COMPLETED
        }
        viewModelScope.launch {
            repo.updateOrderStatus(order.id, nextStatus)
        }
    }

    // ─── Admin Reports ────────────────────────────────────────────────────────
    private val _todayOrders = MutableStateFlow<List<Order>>(emptyList())
    val todayOrders: StateFlow<List<Order>> = _todayOrders

    fun loadTodayOrders() {
        viewModelScope.launch {
            try {
                _todayOrders.value = repo.getTodayOrders()
            } catch (e: Exception) {
                _todayOrders.value = emptyList()
            }
        }
    }

    fun loadOrdersByDateRange(startDate: com.google.firebase.Timestamp, endDate: com.google.firebase.Timestamp) {
        viewModelScope.launch {
            try {
                _todayOrders.value = repo.getOrdersByDateRange(startDate, endDate)
            } catch (e: Exception) {
                _todayOrders.value = emptyList()
            }
        }
    }
}