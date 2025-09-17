package com.doan_adr.smart_order_app.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val id: String = "",
    val dishId: String = "",
    val dishName: String = "",
    var quantity: Int = 1,
    val note: String = "",
    val imageUrl: String? = null,
    val toppings: Map<String, ToppingSelection> = emptyMap(),
    val unitPrice: Double = 0.0,
    var totalPrice: Double = 0.0
) : Parcelable

@Parcelize
data class ToppingSelection(
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
) : Parcelable

fun CartItem.toMap(): Map<String, Any> {
    return hashMapOf(
        "id" to this.id,
        "dishId" to this.dishId,
        "dishName" to this.dishName,
        "quantity" to this.quantity,
        "note" to this.note,
        "imageUrl" to (this.imageUrl ?: ""),
        "toppings" to this.toppings.mapValues { it.value.toMap() },
        "unitPrice" to this.unitPrice,
        "totalPrice" to this.totalPrice
    )
}

fun ToppingSelection.toMap(): Map<String, Any> {
    return hashMapOf(
        "name" to this.name,
        "quantity" to this.quantity,
        "price" to this.price
    )
}