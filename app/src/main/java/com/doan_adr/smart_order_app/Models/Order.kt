package com.doan_adr.smart_order_app.Models

data class Order(
    val id: String = "",
    val tableId: String = "",
    val tableName: String = "", // Giữ lại trường này nếu cần
    val status: String = "pending",
    val createdAt: String = "", // Đã đổi tên từ orderTime
    val cartItems: List<Map<String, Any>> = emptyList(), // Đã thay đổi kiểu dữ liệu để đồng bộ với CartDialogFragment
    val originalTotalPrice: Double = 0.0, // Đã đổi tên từ subtotal
    val discountCode: String? = null,
    val discountValue: Double = 0.0, // Thêm trường discountValue
    val finalTotalPrice: Double = 0.0, // Đã đổi tên từ total
    val paymentMethod: String = "table",
    val paymentStatus: String = "pending",
    val cookingStartTime: String? = null,
    val readyTime: String? = null,
    val servedTime: String? = null,
    val completedTime: String? = null,
    val qrData: String? = null
)

// Giữ nguyên OrderItem và các hàm liên quan nếu cần
data class OrderItem(
    val dishId: String = "",
    val dishName: String = "",
    val quantity: Int = 1,
    val note: String = "",
    val toppings: Map<String, ToppingSelection> = emptyMap(),
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
)

fun Order.toMap(): Map<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "tableId" to this.tableId,
        "tableName" to this.tableName,
        "status" to this.status,
        "createdAt" to this.createdAt,
        "cartItems" to this.cartItems,
        "originalTotalPrice" to this.originalTotalPrice,
        "discountCode" to this.discountCode,
        "discountValue" to this.discountValue,
        "finalTotalPrice" to this.finalTotalPrice,
        "paymentMethod" to this.paymentMethod,
        "paymentStatus" to this.paymentStatus,
        "cookingStartTime" to this.cookingStartTime,
        "readyTime" to this.readyTime,
        "servedTime" to this.servedTime,
        "completedTime" to this.completedTime,
        "qrData" to this.qrData
    )
}

// Giữ nguyên hàm này nếu bạn vẫn sử dụng OrderItem ở các phần khác của ứng dụng
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