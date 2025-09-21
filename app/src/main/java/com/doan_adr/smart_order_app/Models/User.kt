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
    val role: String = "", // "chef" hoặc "manager"
    @ServerTimestamp
    var createdAt: Timestamp? = null
) : Parcelable

fun User.toMap(): Map<String, Any> {
    return hashMapOf(
        "uid" to this.uid,
        "username" to this.username,
        "email" to this.email,
        "role" to this.role,
    )
}