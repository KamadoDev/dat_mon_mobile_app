package com.doan_adr.smart_order_app.Models

data class Order(
    val id: String = "",
    val tableId: String = "",
    val tableName: String = "",
    val status: String = "pending",
    val orderTime: String = "",
    val items: Map<String, OrderItem> = emptyMap(),
    val subtotal: Double = 0.0,
    val discountCode: String? = null,
    val discountValue: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "table",
    val paymentStatus: String = "pending",
    val cookingStartTime: String? = null,
    val readyTime: String? = null,
    val servedTime: String? = null,
    val completedTime: String? = null,
    val qrData: String? = null
)

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
        "orderTime" to this.orderTime,
        "items" to this.items.mapValues { it.value.toMap() },
        "subtotal" to this.subtotal,
        "discountCode" to this.discountCode,
        "discountValue" to this.discountValue,
        "total" to this.total,
        "paymentMethod" to this.paymentMethod,
        "paymentStatus" to this.paymentStatus,
        "cookingStartTime" to this.cookingStartTime,
        "readyTime" to this.readyTime,
        "servedTime" to this.servedTime,
        "completedTime" to this.completedTime,
        "qrData" to this.qrData
    )
}

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