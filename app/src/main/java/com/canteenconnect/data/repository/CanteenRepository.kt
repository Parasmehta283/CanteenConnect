package com.canteenconnect.data.repository

import com.canteenconnect.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class CanteenRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ──────────────────────────────────────────────────────────────────────────
    // AUTH
    // ──────────────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        val doc = db.collection("users").document(uid).get().await()
        doc.toObject(User::class.java)!!.copy(uid = uid)
    }

    fun logout() = auth.signOut()

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(uid = uid)
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID not found")
            
            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = "student"  // Default role is student
            )
            
            db.collection("users").document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MENU
    // ──────────────────────────────────────────────────────────────────────────

    fun observeMenuItems(): Flow<List<MenuItem>> = callbackFlow {
        println("CanteenRepository: Starting menu items listener")
        val listener = db.collection("menuItems")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    println("CanteenRepository: Error loading menu items: ${err.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                println("CanteenRepository: Loaded ${items.size} menu items")
                trySend(items)
            }
        awaitClose {
            println("CanteenRepository: Removing menu items listener")
            listener.remove()
        }
    }

    suspend fun addMenuItem(item: MenuItem) {
        val ref = db.collection("menuItems").document()
        db.collection("menuItems").document(ref.id).set(item.copy(id = ref.id)).await()
    }

    suspend fun updateMenuItem(item: MenuItem) {
        db.collection("menuItems").document(item.id).set(item).await()
    }

    suspend fun deleteMenuItem(itemId: String) {
        db.collection("menuItems").document(itemId).delete().await()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TOKEN
    // ──────────────────────────────────────────────────────────────────────────

    private suspend fun getNextToken(): Int {
        val ref = db.collection("tokens").document("daily")
        return try {
            val snap = ref.get().await()
            val counter = (snap.getLong("counter") ?: 0L).toInt() + 1
            ref.set(mapOf("counter" to counter)).await()
            counter
        } catch (e: Exception) {
            (1..99).random()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ORDERS
    // ──────────────────────────────────────────────────────────────────────────

    suspend fun placeOrder(user: User, cartItems: List<CartItem>): Result<Order> = runCatching {
        // Validate all items are still available
        val unavailableItems = cartItems.filter { !it.menuItem.available }
        if (unavailableItems.isNotEmpty()) {
            throw Exception("Some items in your cart are no longer available: ${unavailableItems.joinToString { it.menuItem.name }}")
        }
        
        val token = getNextToken()
        val orderItems = cartItems.map { cart ->
            OrderItem(
                itemId = cart.menuItem.id,
                itemName = cart.menuItem.name,
                price = cart.menuItem.price,
                quantity = cart.quantity
            )
        }
        val total = orderItems.sumOf { it.subtotal }
        val ref = db.collection("orders").document()
        val order = Order(
            id = ref.id,
            studentId = user.uid,
            studentName = user.name,
            tokenNumber = token,
            status = OrderStatus.PLACED.name,
            totalAmount = total,
            items = orderItems,
            createdAt = Timestamp.now()
        )
        ref.set(order).await()
        order
    }

    fun observeStudentOrders(studentId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snap, err ->
                if (err != null) { 
                    trySend(emptyList()) 
                    return@addSnapshotListener 
                }
                val orders = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                val sorted = orders.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
                trySend(sorted)
            }
        awaitClose { listener.remove() }
    }

    fun observeActiveOrders(): Flow<List<Order>> = callbackFlow {
        // Query only by status to avoid complex index requirements.
        // Sorting will be done in-memory.
        val listener = db.collection("orders")
            .whereIn("status", listOf(OrderStatus.PLACED.name, OrderStatus.PREPARING.name, OrderStatus.READY.name))
            .addSnapshotListener { snap, err ->
                if (err != null) { 
                    // If even this fails (e.g. index still missing for whereIn), return empty list to prevent crash
                    trySend(emptyList())
                    return@addSnapshotListener 
                }
                val orders = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                // Sort in memory: first by status priority, then by token number
                val statusPriority = mapOf(
                    OrderStatus.PLACED.name to 1,
                    OrderStatus.PREPARING.name to 2,
                    OrderStatus.READY.name to 3
                )
                val sorted = orders.sortedWith(compareBy(
                    { statusPriority[it.status] ?: 4 },
                    { it.tokenNumber }
                ))
                trySend(sorted)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        db.collection("orders").document(orderId)
            .update("status", newStatus.name).await()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // REPORTS (Admin)
    // ──────────────────────────────────────────────────────────────────────────

    suspend fun getTodayOrders(): List<Order> {
        return try {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            val startOfDay = Timestamp(cal.time)
            val snap = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startOfDay)
                .get().await()
            snap.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOrdersByDateRange(startDate: Timestamp, endDate: Timestamp): List<Order> {
        return try {
            val snap = db.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .get().await()
            snap.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // IMAGE UPLOAD
    // ──────────────────────────────────────────────────────────────────────────

    suspend fun uploadMenuItemImage(imageBytes: ByteArray): String {
        val fileName = "menu_images/${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putBytes(imageBytes).await()
        return ref.downloadUrl.await().toString()
    }
}
