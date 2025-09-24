package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.OrderItem
import com.doan_adr.smart_order_app.databinding.ItemOrderDetailSimplifiedBinding

/**
 * Adapter này dùng để hiển thị danh sách các món ăn trong một đơn hàng.
 * Nó được sử dụng lồng (nested) bên trong RecyclerView của ManagerOrderFullTrackingAdapter.
 */
class OrderItemsAdapter(private val items: List<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemOrderDetailSimplifiedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemOrderDetailSimplifiedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            // Gán tên món ăn và số lượng
            orderItemNameSimplified.text = item.dishName
            orderItemQuantitySimplified.text = "x ${item.quantity}"
        }
    }

    override fun getItemCount() = items.size
}