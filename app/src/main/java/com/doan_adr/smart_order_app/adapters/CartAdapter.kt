package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.R
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<CartAdapter.CartItemViewHolder>() {

    interface OnItemActionListener {
        fun onQuantityChanged(cartItem: CartItem, newQuantity: Int)
        fun onItemRemoved(cartItem: CartItem)
    }

    class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dishName: TextView = itemView.findViewById(R.id.cart_item_name)
        val toppings: TextView = itemView.findViewById(R.id.cart_item_toppings)
        val note: TextView = itemView.findViewById(R.id.cart_item_note)
        val quantityText: TextView = itemView.findViewById(R.id.quantity_text)
        val totalPrice: TextView = itemView.findViewById(R.id.cart_item_total_price)
        val incrementButton: ImageButton = itemView.findViewById(R.id.increment_button)
        val decrementButton: ImageButton = itemView.findViewById(R.id.decrement_button)
        val removeButton: ImageButton = itemView.findViewById(R.id.remove_item_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_dish, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.dishName.text = cartItem.dishName
        holder.quantityText.text = cartItem.quantity.toString()
        holder.totalPrice.text = formatPrice(cartItem.totalPrice)

        if (cartItem.toppings.isNotEmpty()) {
            val toppingNames = cartItem.toppings.values.joinToString(", ") { it.name }
            holder.toppings.text = "Thêm: $toppingNames"
            holder.toppings.visibility = View.VISIBLE
        } else {
            holder.toppings.visibility = View.GONE
        }

        if (cartItem.note.isNotBlank()) {
            holder.note.text = "Ghi chú: ${cartItem.note}"
            holder.note.visibility = View.VISIBLE
        } else {
            holder.note.visibility = View.GONE
        }

        holder.incrementButton.setOnClickListener {
            listener.onQuantityChanged(cartItem, cartItem.quantity + 1)
        }

        holder.decrementButton.setOnClickListener {
            if (cartItem.quantity > 1) {
                listener.onQuantityChanged(cartItem, cartItem.quantity - 1)
            }
        }

        holder.removeButton.setOnClickListener {
            listener.onItemRemoved(cartItem)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(price)
    }

    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}