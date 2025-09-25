package com.doan_adr.smart_order_app.Models

import com.google.firebase.Timestamp

data class Report(
    val date: Timestamp,
    var revenue: Double,
    var orderCount: Int
)