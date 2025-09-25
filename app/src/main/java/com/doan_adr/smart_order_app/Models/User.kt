package com.doan_adr.smart_order_app.Models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*
import com.google.firebase.Timestamp

@Parcelize
data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val avatar: String = "",
    val role: String = "", // "chef" hoặc "manager"
    val isAccountEnabled: Boolean = true, // Thêm trường mới
    @ServerTimestamp
    var createdAt: Timestamp? = null
) : Parcelable

fun User.toMap(): Map<String, Any> {
    return hashMapOf(
        "uid" to this.uid,
        "username" to this.username,
        "email" to this.email,
        "avatar" to this.avatar,
        "role" to this.role,
        "isAccountEnabled" to this.isAccountEnabled,
    )
}