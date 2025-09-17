package com.doan_adr.smart_order_app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.doan_adr.smart_order_app.R // Hãy đảm bảo đường dẫn R này đúng với project của bạn

class OrderTrackingActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking) // Bạn cần tạo layout này

        val orderId = intent.getStringExtra("orderId")
        val orderIdTextView: TextView = findViewById(R.id.order_id_text_view)

        orderIdTextView.text = "Mã đơn hàng: $orderId"
    }
}