package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.databinding.ItemChefOrderBinding
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.Models.CartItem
import java.text.SimpleDateFormat
import java.util.*

// Lớp DiffUtil.ItemCallback để so sánh các đối tượng Order
class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        // So sánh theo id để xác định cùng một item
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        // So sánh toàn bộ nội dung của item
        return oldItem == newItem
    }
}

// Thay đổi Adapter để kế thừa từ ListAdapter
class ChefOrderAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, ChefOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    class OrderViewHolder(val binding: ItemChefOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemChefOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.binding.apply {
            tvTableNumber.text = "Bàn: ${order.tableName}"
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvOrderTime.text = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""

            // Xóa tất cả view cũ trước khi thêm view mới để tránh lặp lại
            llOrderItems.removeAllViews()

            // Duyệt qua từng món ăn và thêm TextView vào LinearLayout
            order.cartItems.forEach { cartItem ->
                // Tạo TextView cho tên món ăn
                val dishNameTextView = TextView(llOrderItems.context).apply {
                    text = "${cartItem.dishName} (${cartItem.quantity})"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                llOrderItems.addView(dishNameTextView)

                // Thêm TextView cho ghi chú (nếu có)
                if (cartItem.note.isNotBlank()) {
                    val noteTextView = TextView(llOrderItems.context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            // Lấy margin từ dimens
                            leftMargin = resources.getDimensionPixelSize(R.dimen.note_margin_left)
                        }
                        text = "Yêu cầu: ${cartItem.note}"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.gray))
                    }
                    llOrderItems.addView(noteTextView)
                }

                // Thêm TextView cho topping (nếu có)
                val toppings = cartItem.toppings.values.joinToString(", ") { it.name }
                if (toppings.isNotBlank()) {
                    val toppingsTextView = TextView(llOrderItems.context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            // Lấy margin từ dimens
                            leftMargin = resources.getDimensionPixelSize(R.dimen.note_margin_left)
                        }
                        text = "Topping: $toppings"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.gray))
                    }
                    llOrderItems.addView(toppingsTextView)
                }
            }

            // Cập nhật text và màu cho Button
            btnAction.text = when (order.status) {
                "pending" -> "Bắt đầu nấu"
                "cooking" -> "Hoàn thành"
                "ready" -> "Đã phục vụ"
                else -> "Đã xong"
            }
            btnAction.setBackgroundResource(
                when (order.status) {
                    "pending" -> R.color.green
                    "cooking" -> R.color.accent // Đã đổi màu xanh để phân biệt
                    "ready" -> R.color.gray
                    else -> R.color.gray
                }
            )
            btnAction.setOnClickListener {
                onItemClick(order)
            }
        }
    }
}