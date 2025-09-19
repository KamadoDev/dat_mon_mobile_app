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
) : Parcelable {
    companion object
}

@Parcelize
data class ToppingSelection(
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
) : Parcelable {
    companion object
}

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

fun CartItem.Companion.fromMap(map: Map<String, Any>): CartItem {
    return CartItem(
        id = map["id"] as? String ?: "",
        dishId = map["dishId"] as? String ?: "",
        dishName = map["dishName"] as? String ?: "",
        quantity = (map["quantity"] as? Long)?.toInt() ?: 1,
        note = map["note"] as? String ?: "",
        imageUrl = map["imageUrl"] as? String,
        toppings = (map["toppings"] as? Map<String, Map<String, Any>>)?.mapValues {
            ToppingSelection.fromMap(it.value)
        } ?: emptyMap(),
        unitPrice = map["unitPrice"] as? Double ?: 0.0,
        totalPrice = map["totalPrice"] as? Double ?: 0.0
    )
}

fun ToppingSelection.Companion.fromMap(map: Map<String, Any>): ToppingSelection {
    return ToppingSelection(
        name = map["name"] as? String ?: "",
        quantity = (map["quantity"] as? Long)?.toInt() ?: 1,
        price = map["price"] as? Double ?: 0.0
    )
}