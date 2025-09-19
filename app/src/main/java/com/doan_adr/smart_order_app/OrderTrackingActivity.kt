package com.doan_adr.smart_order_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.Models.fromMap
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.OrderDetailsAdapter
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderTrackingActivity : AppCompatActivity() {

    // Khai báo các biến UI
    private lateinit var toolbar: Toolbar
    private lateinit var orderItemsRecyclerView: RecyclerView
    private lateinit var orderDetailsAdapter: OrderDetailsAdapter
    private lateinit var callStaffButton: Button
    private lateinit var cancelOrderButton: Button
    private lateinit var payAtLocationButton: Button

    // Các TextView
    private lateinit var orderIdTextView: TextView
    private lateinit var paymentMethodTextView: TextView
    private lateinit var paymentStatusTextView: TextView
    private lateinit var orderStatusTextView: TextView
    private lateinit var finalTotalPriceTextView: TextView
    private lateinit var placedTimeTextView: TextView
    private lateinit var cookingTimeTextView: TextView
    private lateinit var readyTimeTextView: TextView
    private lateinit var servedTimeTextView: TextView

    // Các ImageView và View
    private lateinit var placedIcon: ImageView
    private lateinit var cookingIcon: ImageView
    private lateinit var readyIcon: ImageView
    private lateinit var servedIcon: ImageView
    private lateinit var linePlacedCooking: View
    private lateinit var lineCookingReady: View
    private lateinit var lineReadyServed: View
    private lateinit var cookingLayout: LinearLayout
    private lateinit var readyLayout: LinearLayout
    private lateinit var servedLayout: LinearLayout

    // Các biến logic
    private var orderListener: ListenerRegistration? = null
    private lateinit var databaseManager: FirebaseDatabaseManager
    private lateinit var orderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        // Lấy orderId từ Intent
        orderId = intent.getStringExtra("orderId") ?: run {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ánh xạ các thành phần UI
        bindUIComponents()

        // Khởi tạo Adapter và RecyclerView
        orderDetailsAdapter = OrderDetailsAdapter(emptyList())
        orderItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        orderItemsRecyclerView.adapter = orderDetailsAdapter

        // Khởi tạo DatabaseManager
        databaseManager = FirebaseDatabaseManager()

        // Thiết lập listener cho các nút
        setupButtonListeners()

        // Lắng nghe trạng thái đơn hàng từ Firestore
        listenForOrderStatus(orderId)

        // Cập nhật giao diện ban đầu
        orderIdTextView.text = "Mã đơn hàng: $orderId"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun bindUIComponents() {
        toolbar = findViewById(R.id.toolbar)
        orderItemsRecyclerView = findViewById(R.id.order_items_recycler_view)
        callStaffButton = findViewById(R.id.call_staff_button)
        cancelOrderButton = findViewById(R.id.cancel_order_button)
        payAtLocationButton = findViewById(R.id.pay_at_location_button)

        orderIdTextView = findViewById(R.id.order_id_text_view)
        paymentMethodTextView = findViewById(R.id.payment_method_text_view)
        paymentStatusTextView = findViewById(R.id.payment_status_text_view)
        orderStatusTextView = findViewById(R.id.order_status_text_view)
        finalTotalPriceTextView = findViewById(R.id.final_total_price_text_view)
        placedTimeTextView = findViewById(R.id.placed_time_text_view)
        cookingTimeTextView = findViewById(R.id.cooking_time_text_view)
        readyTimeTextView = findViewById(R.id.ready_time_text_view)
        servedTimeTextView = findViewById(R.id.served_time_text_view)

        placedIcon = findViewById(R.id.placed_icon)
        cookingIcon = findViewById(R.id.cooking_icon)
        readyIcon = findViewById(R.id.ready_icon)
        servedIcon = findViewById(R.id.served_icon)
        linePlacedCooking = findViewById(R.id.line_placed_cooking)
        lineCookingReady = findViewById(R.id.line_cooking_ready)
        lineReadyServed = findViewById(R.id.line_ready_served)

        cookingLayout = findViewById(R.id.cooking_layout)
        readyLayout = findViewById(R.id.ready_layout)
        servedLayout = findViewById(R.id.served_layout)
    }

    // Trong OrderTrackingActivity.kt
    private fun setupButtonListeners() {
        // Nút Gọi nhân viên không cần thay đổi
        callStaffButton.setOnClickListener {
            val phoneNumber = "tel:123456789"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
            startActivity(intent)
        }

        cancelOrderButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hủy Đơn Hàng")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy") { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            databaseManager.cancelOrder(orderId)
                            Toast.makeText(this@OrderTrackingActivity, "Đơn hàng đã được hủy.", Toast.LENGTH_SHORT).show()
                            finish()
                        } catch (e: Exception) {
                            // Hiển thị thông báo lỗi cụ thể từ FirebaseDatabaseManager
                            Toast.makeText(this@OrderTrackingActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Không") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        payAtLocationButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Thanh Toán Tại Chỗ")
                .setMessage("Xác nhận đã thanh toán tiền mặt và hoàn thành đơn hàng?")
                .setPositiveButton("Xác nhận") { dialog, _ ->
                    lifecycleScope.launch {
                        try {
                            databaseManager.updatePaymentAndCompletion(orderId)
                            Toast.makeText(this@OrderTrackingActivity, "Đã cập nhật thanh toán.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // Hiển thị thông báo lỗi cụ thể từ FirebaseDatabaseManager
                            Toast.makeText(this@OrderTrackingActivity, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Không") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
                    val finalTotalPrice = snapshot.getDouble("finalTotalPrice") ?: 0.0

                    val cartItemsData = snapshot.get("cartItems") as? List<Map<String, Any>>
                    val cartItems = cartItemsData?.map { CartItem.fromMap(it) } ?: emptyList()

                    val placedTime = snapshot.getTimestamp("createdAt")?.toDate()
                    val cookingTime = snapshot.getTimestamp("cookingStartTime")?.toDate()
                    val readyTime = snapshot.getTimestamp("readyTime")?.toDate()
                    val servedTime = snapshot.getTimestamp("servedTime")?.toDate()

                    updateUI(status, paymentMethod, paymentStatus, placedTime, cookingTime, readyTime, servedTime, finalTotalPrice, cartItems)
                    updateButtonVisibility(status, paymentMethod)
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
        servedTime: Date?,
        finalTotalPrice: Double,
        cartItems: List<CartItem>
    ) {
        orderStatusTextView.text = getVietnameseStatus(status)
        finalTotalPriceTextView.text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(finalTotalPrice)
        paymentMethodTextView.text = getVietnamesePaymentMethod(paymentMethod)
        paymentStatusTextView.text = getVietnamesePaymentStatus(paymentStatus)
        orderDetailsAdapter.updateData(cartItems)

        val greenColor = ContextCompat.getColorStateList(this, R.color.green)
        val grayColor = ContextCompat.getColorStateList(this, R.color.gray)
        // Thay đổi định dạng thời gian để hiển thị cả ngày và giờ
        val timeFormat = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())

        // Cập nhật màu sắc và thời gian của timeline
        placedIcon.imageTintList = greenColor
        placedTimeTextView.text = placedTime?.let { timeFormat.format(it) } ?: "Chưa có"


        when (status) {
            "pending", "pending_online" -> {
                // Đã xử lý ở trên
            }
            "cooking" -> {
                cookingIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cookingTimeTextView.text = cookingTime?.let { timeFormat.format(it) } ?: "Chưa có"
            }
            "ready" -> {
                cookingIcon.imageTintList = greenColor
                readyIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineCookingReady.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cookingTimeTextView.text = cookingTime?.let { timeFormat.format(it) } ?: "Chưa có"
                readyTimeTextView.text = readyTime?.let { timeFormat.format(it) } ?: "Chưa có"
            }
            "served" -> {
                cookingIcon.imageTintList = greenColor
                readyIcon.imageTintList = greenColor
                servedIcon.imageTintList = greenColor
                linePlacedCooking.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineCookingReady.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                lineReadyServed.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                cookingTimeTextView.text = cookingTime?.let { timeFormat.format(it) } ?: "Chưa có"
                readyTimeTextView.text = readyTime?.let { timeFormat.format(it) } ?: "Chưa có"
                servedTimeTextView.text = servedTime?.let { timeFormat.format(it) } ?: "Chưa có"
            }
        }
    }

    private fun updateButtonVisibility(status: String, paymentMethod: String) {
        // Cập nhật trạng thái của các nút dựa trên trạng thái đơn hàng
        if (status == "pending" || status == "pending_online") {
            // Có thể hủy đơn hàng
            cancelOrderButton.visibility = View.VISIBLE
            // Không thể thanh toán
            payAtLocationButton.visibility = View.GONE
        } else if (status == "served" && paymentMethod == "cash") {
            // Có thể thanh toán tại chỗ nếu đơn hàng đã được phục vụ và là thanh toán tiền mặt
            payAtLocationButton.visibility = View.VISIBLE
            // Không thể hủy đơn hàng
            cancelOrderButton.visibility = View.GONE
        } else {
            // Đã cooking, ready, served (và đã thanh toán) hoặc completed: không cho phép hủy hay thanh toán
            cancelOrderButton.visibility = View.GONE
            payAtLocationButton.visibility = View.GONE
        }
    }

    private fun getVietnamesePaymentMethod(method: String): String {
        return when (method) {
            "cash" -> "Tiền mặt"
            "online" -> "Thanh toán trực tuyến"
            else -> "N/A"
        }
    }

    private fun getVietnamesePaymentStatus(status: String): String {
        return when (status) {
            "pending" -> "Chưa thanh toán"
            "pending_online" -> "Chưa thanh toán"
            "paid" -> "Đã thanh toán"
            else -> "N/A"
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
        orderListener?.remove()
    }
}