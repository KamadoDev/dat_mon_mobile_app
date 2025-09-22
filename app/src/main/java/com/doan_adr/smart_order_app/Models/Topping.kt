package com.doan_adr.smart_order_app.Models

data class Topping(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0
)

fun Topping.toMap(): Map<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "name" to this.name,
        "price" to this.price
    )
}