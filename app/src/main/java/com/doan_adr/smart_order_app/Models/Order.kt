package com.doan_adr.smart_order_app.Models

import android.os.Parcelable
import com.doan_adr.smart_order_app.utils.TimestampParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.util.*
// Import cần thiết
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

@Parcelize
@TypeParceler<Timestamp, TimestampParceler>
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val tableId: String = "",
    val tableName: String = "",
    val status: String = "pending", // cooking - ready - served - completed - cancelled
    @ServerTimestamp
    var createdAt: Timestamp? = null,
    val cartItems: List<CartItem> = emptyList(), // Sử dụng CartItem từ file CartItem.kt
    val originalTotalPrice: Double = 0.0,
    val discountCode: String? = null,
    val discountValue: Double = 0.0,
    val finalTotalPrice: Double = 0.0,
    val paymentMethod: String = "cash",
    val paymentStatus: String = "pending",
    val cookingStartTime: Timestamp? = null,
    val readyTime: Timestamp? = null,
    val servedTime: Timestamp? = null,
    val completedTime: Timestamp? = null,
    val cancelledTime: Timestamp? = null,
    val qrData: String? = null,
    val cookingChefId: String? = null, // UID đầu bếp
    val cookingChefName: String? = null // lưu tên đầu bếp
) : Parcelable

/**
 * Lớp dữ liệu đại diện cho một món ăn trong đơn hàng.
 * Lưu ý: Lớp này cần khớp với CartItem.kt để đồng bộ dữ liệu.
 */
@Parcelize
data class OrderItem(
    val dishId: String = "",
    val dishName: String = "",
    val quantity: Int = 1,
    val note: String = "",
    val toppings: Map<String, ToppingSelection> = emptyMap(),
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
) : Parcelable

/**
 * Hàm mở rộng để chuyển đổi đối tượng Order sang Map để lưu trữ trên Firestore.
 */
fun Order.toMap(): Map<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "tableId" to this.tableId,
        "tableName" to this.tableName,
        "status" to this.status,
        "cartItems" to this.cartItems.map { it.toMap() }, // Chuyển đổi CartItem sang Map
        "originalTotalPrice" to this.originalTotalPrice,
        "discountCode" to this.discountCode,
        "discountValue" to this.discountValue,
        "finalTotalPrice" to this.finalTotalPrice,
        "paymentMethod" to this.paymentMethod,
        "paymentStatus" to this.paymentStatus,
        "qrData" to this.qrData,
        "cookingChefId" to this.cookingChefId,
        "cookingChefName" to this.cookingChefName
    )
}

/**
 * Hàm mở rộng để chuyển đổi đối tượng OrderItem sang Map.
 * Lưu ý: Có thể sử dụng lại hàm toMap() của CartItem nếu hai lớp này đồng bộ.
 */
fun OrderItem.toMap(): Map<String, Any> {
    return hashMapOf(
        "dishId" to this.dishId,
        "dishName" to this.dishName,
        "quantity" to this.quantity,
        "note" to this.note,
        "toppings" to this.toppings.mapValues { it.value.toMap() },
        "unitPrice" to this.unitPrice,
        "totalPrice" to this.totalPrice
    )
}

fun CartItem.toOrderItem(): OrderItem {
    return OrderItem(
        dishId = this.dishId,
        dishName = this.dishName,
        quantity = this.quantity,
        note = this.note,
        toppings = this.toppings,
        unitPrice = this.unitPrice,
        totalPrice = this.totalPrice
    )
}
