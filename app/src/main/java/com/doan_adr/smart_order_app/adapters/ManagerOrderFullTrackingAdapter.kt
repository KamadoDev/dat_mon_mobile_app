package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.Models.toOrderItem // Import hàm mở rộng
import com.doan_adr.smart_order_app.databinding.ItemManagerOrderFullTrackingBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManagerOrderFullTrackingAdapter(
    private val orders: MutableList<Order>
) : RecyclerView.Adapter<ManagerOrderFullTrackingAdapter.OrderViewHolder>() {

    private val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    private val timeFormatter = SimpleDateFormat("HH:mm, dd/MM", Locale("vi", "VN"))

    class OrderViewHolder(val binding: ItemManagerOrderFullTrackingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemManagerOrderFullTrackingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            tvOrderId.text = "Đơn: ${order.id.substring(0, 8)}"
            tvTableInfo.text = "Bàn: ${order.tableName}"
            tvOrderTime.text = "Đặt lúc: ${timeFormatter.format(order.createdAt?.toDate() ?: Date())}"
            tvOrderTotal.text = "Tổng tiền: ${format.format(order.finalTotalPrice)}đ"
            tvOrderStatus.text = "Trạng thái: ${getStatusText(order.status, order.cookingChefName)}"

            // Thiết lập RecyclerView cho các món ăn
            // Lỗi đã được sửa ở đây
            val itemAdapter = OrderItemsAdapter(order.cartItems.map { it.toOrderItem() })
            orderItemsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = itemAdapter
                isNestedScrollingEnabled = false
            }

            // Hiển thị lịch sử trạng thái
            tvHistoryDetails.text = buildHistoryText(order)
        }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders.clear()
        orders.addAll(newOrders)
        notifyDataSetChanged()
    }

    private fun getStatusText(status: String, chefName: String?): String {
        return when (status) {
            "pending" -> "Đang chờ"
            "cooking" -> "Đang nấu${if (chefName != null) " (Đầu bếp: $chefName)" else ""}"
            "ready" -> "Đã sẵn sàng"
            "served" -> "Đã phục vụ"
            "completed" -> "Đã hoàn thành"
            "cancelled" -> "Đã hủy"
            else -> "Không xác định"
        }
    }

    private fun buildHistoryText(order: Order): String {
        val history = StringBuilder()
        val timeFormatter = SimpleDateFormat("HH:mm", Locale("vi", "VN"))

        order.createdAt?.let {
            history.append("• Đặt lúc: ${timeFormatter.format(it.toDate())}\n")
        }
        order.cookingStartTime?.let {
            history.append("• Bắt đầu nấu: ${timeFormatter.format(it.toDate())}")
            if (order.cookingChefName != null) {
                history.append(" (Đầu bếp: ${order.cookingChefName})")
            }
            history.append("\n")
        }
        order.readyTime?.let {
            history.append("• Sẵn sàng: ${timeFormatter.format(it.toDate())}\n")
        }
        order.servedTime?.let {
            history.append("• Đã phục vụ: ${timeFormatter.format(it.toDate())}\n")
        }
        order.completedTime?.let {
            history.append("• Hoàn thành: ${timeFormatter.format(it.toDate())}\n")
        }
        order.cancelledTime?.let {
            history.append("• Đã hủy: ${timeFormatter.format(it.toDate())}\n")
        }
        return history.toString().trim()
    }
}