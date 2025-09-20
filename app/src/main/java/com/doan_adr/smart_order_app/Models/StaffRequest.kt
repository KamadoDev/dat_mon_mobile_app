package com.doan_adr.smart_order_app.Models

import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

data class StaffRequest(
    val orderId: String = "",
    val tableId: String = "",
    val requestType: String = "",
    val isHandled: Boolean = false,
    @ServerTimestamp
    var timestamp: Timestamp? = null
)