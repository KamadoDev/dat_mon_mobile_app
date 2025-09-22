package com.doan_adr.smart_order_app.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log // Import lớp Log
import android.view.LayoutInflater
import android.view.View
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

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}

class ChefOrderAdapter(
    private val onItemClick: (Order) -> Unit
) : ListAdapter<Order, ChefOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    class OrderViewHolder(val binding: ItemChefOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemChefOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.binding.apply {
            tvTableNumber.text = "Bàn: ${order.tableName}"
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvOrderTime.text = order.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""

            llOrderItems.removeAllViews()

            order.cartItems.forEach { cartItem ->
                val dishNameTextView = TextView(llOrderItems.context).apply {
                    text = "${cartItem.dishName} (${cartItem.quantity})"
                    textSize = 16f
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                llOrderItems.addView(dishNameTextView)

                if (cartItem.note.isNotBlank()) {
                    val noteTextView = TextView(llOrderItems.context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            leftMargin = resources.getDimensionPixelSize(R.dimen.note_margin_left)
                        }
                        text = "Yêu cầu: ${cartItem.note}"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.gray))
                    }
                    llOrderItems.addView(noteTextView)
                }

                val toppings = cartItem.toppings.values.joinToString(", ") { it.name }
                if (toppings.isNotBlank()) {
                    val toppingsTextView = TextView(llOrderItems.context).apply {
                        layoutParams = ViewGroup.MarginLayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            leftMargin = resources.getDimensionPixelSize(R.dimen.note_margin_left)
                        }
                        text = "Topping: $toppings"
                        textSize = 14f
                        setTextColor(ContextCompat.getColor(context, R.color.gray))
                    }
                    llOrderItems.addView(toppingsTextView)
                }
            }

            btnAction.text = when (order.status) {
                "pending" -> "Bắt đầu nấu"
                "cooking" -> "Hoàn thành"
                "ready" -> "Đã phục vụ"
                else -> "Đã xong"
            }
            btnAction.backgroundTintList = when (order.status) {
                "pending" -> ColorStateList.valueOf(ContextCompat.getColor(btnAction.context, R.color.green))
                "cooking" -> ColorStateList.valueOf(ContextCompat.getColor(btnAction.context, R.color.accent))
                "ready" -> ColorStateList.valueOf(ContextCompat.getColor(btnAction.context, R.color.gray))
                else -> ColorStateList.valueOf(ContextCompat.getColor(btnAction.context, R.color.gray))
            }

            btnAction.setOnClickListener {
                onItemClick(order)
            }

            // Thêm các lệnh log để kiểm tra giá trị của các trường
            Log.d("ChefOrderAdapter", "Order ID: ${order.id}")
            Log.d("ChefOrderAdapter", "Status: ${order.status}")
            Log.d("ChefOrderAdapter", "cookingChefName: ${order.cookingChefName}")
            Log.d("ChefOrderAdapter", "cookingChefId: ${order.cookingChefId}")

            // Hiển thị thông tin đầu bếp nếu trạng thái là "cooking" hoặc "ready"
            if (order.status == "cooking" || order.status == "ready") {
                // Hiển thị cả container cha và các TextView con
                chefInfoContainer.visibility = View.VISIBLE // Thêm dòng này
                tvChefName.visibility = View.VISIBLE
                tvChefUid.visibility = View.VISIBLE

                tvChefName.text = "Tiếp nhận: ${order.cookingChefName}"
                tvChefUid.text = "UID: ${order.cookingChefId}"
            } else {
                // Ẩn cả container cha và các TextView con
                chefInfoContainer.visibility = View.GONE // Thêm dòng này
                tvChefName.visibility = View.GONE
                tvChefUid.visibility = View.GONE
            }
        }
    }
}