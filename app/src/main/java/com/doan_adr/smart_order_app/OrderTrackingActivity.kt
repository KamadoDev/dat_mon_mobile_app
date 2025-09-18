package com.doan_adr.smart_order_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.doan_adr.smart_order_app.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var orderIdTextView: TextView
    private lateinit var paymentMethodTextView: TextView
    private lateinit var paymentStatusTextView: TextView
    private lateinit var orderStatusTextView: TextView

    private lateinit var placedIcon: ImageView
    private lateinit var cookingIcon: ImageView
    private lateinit var readyIcon: ImageView
    private lateinit var servedIcon: ImageView

    private lateinit var placedTimeTextView: TextView
    private lateinit var cookingTimeTextView: TextView
    private lateinit var readyTimeTextView: TextView
    private lateinit var servedTimeTextView: TextView

    private lateinit var linePlacedCooking: View
    private lateinit var lineCookingReady: View
    private lateinit var lineReadyServed: View

    private lateinit var cookingLayout: LinearLayout
    private lateinit var readyLayout: LinearLayout
    private lateinit var servedLayout: LinearLayout

    private var orderListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        orderIdTextView = findViewById(R.id.order_id_text_view)
        paymentMethodTextView = findViewById(R.id.payment_method_text_view)
        paymentStatusTextView = findViewById(R.id.payment_status_text_view)
        orderStatusTextView = findViewById(R.id.order_status_text_view)

        placedIcon = findViewById(R.id.placed_icon)
        cookingIcon = findViewById(R.id.cooking_icon)
        readyIcon = findViewById(R.id.ready_icon)
        servedIcon = findViewById(R.id.served_icon)

        placedTimeTextView = findViewById(R.id.placed_time_text_view)
        cookingTimeTextView = findViewById(R.id.cooking_time_text_view)
        readyTimeTextView = findViewById(R.id.ready_time_text_view)
        servedTimeTextView = findViewById(R.id.served_time_text_view)

        linePlacedCooking = findViewById(R.id.line_placed_cooking)
        lineCookingReady = findViewById(R.id.line_cooking_ready)
        lineReadyServed = findViewById(R.id.line_ready_served)

        cookingLayout = findViewById(R.id.cooking_layout)
        readyLayout = findViewById(R.id.ready_layout)
        servedLayout = findViewById(R.id.served_layout)

        val orderId = intent.getStringExtra("orderId")
        if (orderId != null) {
            orderIdTextView.text = "Mã đơn hàng: $orderId"
            listenForOrderStatus(orderId)
        } else {
            orderIdTextView.text = "Không tìm thấy mã đơn hàng."
        }
    }

    private fun listenForOrderStatus(orderId: String) {
        val db = FirebaseFirestore.getInstance()
        orderListener = db.collection("orders").document(orderId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("OrderTracking", "Lỗi lắng nghe trạng thái đơn hàng: ", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val status = snapshot.getString("status") ?: "N/A"
                    val paymentMethod = snapshot.getString("paymentMethod") ?: "N/A"
                    val paymentStatus = snapshot.getString("paymentStatus") ?: "N/A"

                    val placedTime = snapshot.getTimestamp("createdAt")?.toDate()
                    val cookingTime = snapshot.getTimestamp("cookingStartTime")?.toDate()
                    val readyTime = snapshot.getTimestamp("readyTime")?.toDate()
                    val servedTime = snapshot.getTimestamp("servedTime")?.toDate()

                    updateUI(status, paymentMethod, paymentStatus, placedTime, cookingTime, readyTime, servedTime)
                }
            }
    }

    private fun updateUI(
        status: String,
        paymentMethod: String,
        paymentStatus: String,
        placedTime: Date?,
        cookingTime: Date?,
        readyTime: Date?,
        servedTime: Date?
    ) {
        val dateFormat = SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault())

        // Cập nhật thông tin cơ bản
        paymentMethodTextView.text = "Thanh toán: ${if (paymentMethod == "cash") "Tiền mặt" else "Thanh toán online"}"
        paymentStatusTextView.text = "Trạng thái: ${if (paymentStatus == "paid") "Đã thanh toán" else "Chưa thanh toán"}"
        orderStatusTextView.text = "Trạng thái: ${getVietnameseStatus(status)}"

        // Cập nhật timeline
        // Reset tất cả về trạng thái chưa hoàn thành
        val grayColor = ContextCompat.getColorStateList(this, R.color.gray)
        val greenColor = ContextCompat.getColorStateList(this, R.color.green)

        placedIcon.imageTintList = grayColor
        cookingIcon.imageTintList = grayColor
        readyIcon.imageTintList = grayColor
        servedIcon.imageTintList = grayColor

        linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        lineCookingReady.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
        lineReadyServed.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))

        placedTimeTextView.text = placedTime?.let { dateFormat.format(it) } ?: "Chưa bắt đầu"
        cookingTimeTextView.text = cookingTime?.let { dateFormat.format(it) } ?: "Chưa bắt đầu"
        readyTimeTextView.text = readyTime?.let { dateFormat.format(it) } ?: "Chưa sẵn sàng"
        servedTimeTextView.text = servedTime?.let { dateFormat.format(it) } ?: "Chưa phục vụ"

        // Cập nhật trạng thái dựa trên status
        when (status) {
            "pending", "pending_online" -> {
                // Đã đặt hàng
                placedIcon.imageTintList = greenColor
            }
            "cooking" -> {
                // Đã đặt hàng và đang nấu
                placedIcon.imageTintList = greenColor
                cookingIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            "ready" -> {
                // Đã đặt, đang nấu, đã sẵn sàng
                placedIcon.imageTintList = greenColor
                cookingIcon.imageTintList = greenColor
                readyIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineCookingReady.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            "served" -> {
                // Đã hoàn thành tất cả
                placedIcon.imageTintList = greenColor
                cookingIcon.imageTintList = greenColor
                readyIcon.imageTintList = greenColor
                servedIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineCookingReady.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineReadyServed.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
        }
    }

    private fun getVietnameseStatus(status: String): String {
        return when (status) {
            "pending", "pending_online" -> "Đang chờ xử lý"
            "cooking" -> "Đang nấu ăn"
            "ready" -> "Đã sẵn sàng"
            "served" -> "Đã phục vụ"
            else -> "Không xác định"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        orderListener?.remove() // Dừng lắng nghe khi Activity bị hủy
    }
}
