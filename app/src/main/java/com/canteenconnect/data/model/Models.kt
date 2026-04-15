package com.canteenconnect.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

// ─── User ───────────────────────────────────────────────────────────────────
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "student"   // "student" | "staff" | "admin"
)

// ─── Menu ────────────────────────────────────────────────────────────────────
data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val available: Boolean = true,
    val category: String = "Main",
    val imageUrl: String = ""
)

// ─── Cart ────────────────────────────────────────────────────────────────────
data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int = 1
) {
    @get:Exclude
    val subtotal: Double get() = menuItem.price * quantity
}

// ─── Order ───────────────────────────────────────────────────────────────────
data class OrderItem(
    val itemId: String = "",
    val itemName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
) {
    @get:Exclude
    val subtotal: Double get() = price * quantity
}

enum class OrderStatus { PLACED, PREPARING, READY, COMPLETED }

data class Order(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val tokenNumber: Int = 0,
    val status: String = OrderStatus.PLACED.name,
    val totalAmount: Double = 0.0,
    val items: List<OrderItem> = emptyList(),
    val createdAt: Timestamp? = null
)
