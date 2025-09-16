package com.doan_adr.smart_order_app.Models

import com.google.firebase.firestore.DocumentSnapshot

data class Table(
    val id: String = "",
    val name: String = "",
    val status: String = "", // "available", "occupied"
    val currentOrderId: String? = null,
    val tableNumber: Int = 0 // Thêm trường này để sắp xếp
)

fun Table.toMap(): Map<String, Any?> {
    return hashMapOf(
        "id" to this.id,
        "name" to this.name,
        "status" to this.status,
        "currentOrderId" to this.currentOrderId,
        "tableNumber" to this.tableNumber // Thêm trường này vào HashMap
    )
}

fun DocumentSnapshot.toTable(): Table {
    val id = this.id
    val name = this.getString("name") ?: ""
    val status = this.getString("status") ?: ""
    val currentOrderId = this.getString("currentOrderId")
    val tableNumber = this.getLong("tableNumber")?.toInt() ?: 0 // Đọc trường mới
    return Table(id, name, status, currentOrderId, tableNumber)
}