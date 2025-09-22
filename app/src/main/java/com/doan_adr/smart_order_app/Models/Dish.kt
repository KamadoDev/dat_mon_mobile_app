package com.doan_adr.smart_order_app.Models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Dish(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val originalPrice: Double = 0.0,
    val discountedPrice: Double = 0.0,
    val categoryId: String = "",
    val imageUrl: String = "",
    val healthTips: String = "",
    val isAvailable: Boolean = true,
    val toppingsAvailable: Boolean = false,
    val toppingIds: List<String> = emptyList()
): Parcelable

fun Dish.toMap(): Map<String, Any> {
    return hashMapOf(
        "id" to this.id,
        "name" to this.name,
        "description" to this.description,
        "originalPrice" to this.originalPrice,
        "discountedPrice" to this.discountedPrice,
        "categoryId" to this.categoryId,
        "imageUrl" to this.imageUrl,
        "healthTips" to this.healthTips,
        "isAvailable" to this.isAvailable,
        "toppingsAvailable" to this.toppingsAvailable,
        "toppingIds" to this.toppingIds
    )
}