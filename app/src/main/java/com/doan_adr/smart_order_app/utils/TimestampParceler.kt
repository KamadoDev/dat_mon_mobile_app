package com.doan_adr.smart_order_app.utils

import android.os.Parcel
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parceler
import java.util.*

object TimestampParceler : Parceler<Timestamp> {
    override fun create(parcel: Parcel): Timestamp {
        val milliseconds = parcel.readLong()
        return Timestamp(Date(milliseconds))
    }

    override fun Timestamp.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.toDate().time)
    }
}