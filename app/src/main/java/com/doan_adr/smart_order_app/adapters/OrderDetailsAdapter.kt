package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.Models.CartItem
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class OrderDetailsAdapter(private var cartItems: List<CartItem>) :
    RecyclerView.Adapter<OrderDetailsAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishImage: ImageView = view.findViewById(R.id.order_item_image)
        val dishName: TextView = view.findViewById(R.id.order_item_name)
        val toppings: TextView = view.findViewById(R.id.order_item_toppings)
        val note: TextView = view.findViewById(R.id.order_item_note)
        val quantity: TextView = view.findViewById(R.id.order_item_quantity)
        val totalPrice: TextView = view.findViewById(R.id.order_item_total_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = cartItems[position]

        holder.dishName.text = item.dishName
        holder.quantity.text = "x ${item.quantity}" // Đây là dòng hiển thị số lượng

        // Định dạng tiền tệ
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        format.maximumFractionDigits = 0
        holder.totalPrice.text = format.format(item.totalPrice)

        // Xử lý toppings và note
        val toppingText = item.toppings.values.joinToString(", ") { it.name }
        if (toppingText.isNotEmpty()) {
            holder.toppings.text = toppingText
            holder.toppings.visibility = View.VISIBLE
        } else {
            holder.toppings.visibility = View.GONE
        }

        if (item.note.isNotEmpty()) {
            holder.note.text = item.note
            holder.note.visibility = View.VISIBLE
        } else {
            holder.note.visibility = View.GONE
        }

        // Tải hình ảnh món ăn
        Glide.with(holder.itemView.context)
            .load(item.imageUrl) // Giả sử CartItem có trường imageUrl
            .placeholder(R.drawable.ic_launcher_background) // Thêm ảnh placeholder nếu cần
            .into(holder.dishImage)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateData(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}