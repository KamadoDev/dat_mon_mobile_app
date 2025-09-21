package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager

class ChefOrderAdapter(
    private var orders: List<Order>,
    private val onActionClick: (Order) -> Unit // Callback khi nhấn nút hành động
) : RecyclerView.Adapter<ChefOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTableNumber: TextView = itemView.findViewById(R.id.tv_table_number)
        val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        val llOrderItems: ViewGroup = itemView.findViewById(R.id.ll_order_items)
        val btnAction: Button = itemView.findViewById(R.id.btn_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chef_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvTableNumber.text = "Bàn ${order.tableName}"
        // Format thời gian hiển thị cho dễ đọc
        val timeString = order.createdAt?.toDate()?.let {
            android.text.format.DateFormat.format("HH:mm", it).toString()
        } ?: "N/A"
        holder.tvOrderTime.text = timeString

        // Xóa các view cũ để tránh trùng lặp khi tái sử dụng viewholder
        holder.llOrderItems.removeAllViews()

        // Thêm các món ăn vào LinearLayout
        order.cartItems.forEach { item ->
            addOrderItemView(holder.llOrderItems, item)
        }

        // Cập nhật text của nút hành động dựa trên trạng thái đơn hàng
        updateActionButton(holder.btnAction, order)

        holder.btnAction.setOnClickListener {
            onActionClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    // Cập nhật danh sách đơn hàng
    fun updateOrders(newOrders: List<Order>) {
        this.orders = newOrders
        notifyDataSetChanged()
    }

    private fun addOrderItemView(parent: ViewGroup, cartItem: CartItem) {
        val inflater = LayoutInflater.from(parent.context)
        val itemLayout = inflater.inflate(R.layout.item_order_dish, parent, false)

        val dishNameTextView: TextView = itemLayout.findViewById(R.id.tv_dish_name)
        val noteTextView: TextView = itemLayout.findViewById(R.id.tv_dish_note)
        val toppingsTextView: TextView = itemLayout.findViewById(R.id.tv_dish_toppings)

        dishNameTextView.text = "${cartItem.dishName} (${cartItem.quantity})"

        // Hiển thị ghi chú nếu có
        if (cartItem.note.isNotEmpty()) {
            noteTextView.text = "Ghi chú: ${cartItem.note}"
            noteTextView.visibility = View.VISIBLE
        } else {
            noteTextView.visibility = View.GONE
        }

        // Hiển thị toppings nếu có
        if (cartItem.toppings.isNotEmpty()) {
            val toppings = cartItem.toppings.values.joinToString(", ") { it.toString() }
            toppingsTextView.text = "Topping: $toppings"
            toppingsTextView.visibility = View.VISIBLE
        } else {
            toppingsTextView.visibility = View.GONE
        }

        parent.addView(itemLayout)
    }

    private fun updateActionButton(button: Button, order: Order) {
        val dbManager = FirebaseDatabaseManager()
        Toast.makeText(button.context, "Đơn hàng: ${order.id}", Toast.LENGTH_SHORT).show()
    }
}