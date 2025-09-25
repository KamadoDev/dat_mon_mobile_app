package com.doan_adr.smart_order_app.Models

data class Discount(
    val id: String? = null,
    val code: String = "",
    val discountType: String = "percentage",
    val value: Double = 0.0,
    val minOrderValue: Double = 0.0,
    val maxDiscount: Double = 0.0,
    val validUntil: String = "",
    val usageLimit: Int = 0,
    // Thêm trường này
    val timesUsed: Int = 0
)

fun Discount.toMap(): Map<String, Any?> {
    return hashMapOf(
        "code" to this.code,
        "discountType" to this.discountType,
        "value" to this.value,
        "minOrderValue" to this.minOrderValue,
        "maxDiscount" to this.maxDiscount,
        "validUntil" to this.validUntil,
        "usageLimit" to this.usageLimit,
        "timesUsed" to this.timesUsed
    )
}
