package com.doan_adr.smart_order_app.Models

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String? = ""
)

fun Category.toMap(): Map<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "name" to this.name,
        "imageUrl" to this.imageUrl,
    )
}
