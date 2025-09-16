package com.doan_adr.smart_order_app.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.R
import java.text.NumberFormat
import java.util.Locale

class MenuAdapter(
    private val context: Context,
    private var dishes: List<Dish>,
    private val onItemClick: (Dish) -> Unit
) : RecyclerView.Adapter<MenuAdapter.DishViewHolder>() {

    class DishViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dishImage: ImageView = view.findViewById(R.id.dish_image)
        val dishName: TextView = view.findViewById(R.id.dish_name)
        val dishOriginalPrice: TextView = view.findViewById(R.id.dish_original_price) // Ánh xạ giá gốc
        val dishDiscountedPrice: TextView = view.findViewById(R.id.dish_discounted_price) // Ánh xạ giá giảm giá
        val dishDescription: TextView = view.findViewById(R.id.dish_description)
        val dishHealthTips: TextView = view.findViewById(R.id.dish_health_tips) // Ánh xạ lời khuyên sức khỏe
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dish, parent, false)
        return DishViewHolder(view)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishes[position]
        holder.dishName.text = dish.name
        holder.dishDescription.text = dish.description

        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // Hiển thị giá gốc và giá giảm giá
        if (dish.originalPrice > dish.discountedPrice) {
            // Có giảm giá
            holder.dishOriginalPrice.text = format.format(dish.originalPrice)
            holder.dishOriginalPrice.paintFlags = holder.dishOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Gạch ngang giá gốc
            holder.dishOriginalPrice.visibility = View.VISIBLE
            holder.dishDiscountedPrice.text = format.format(dish.discountedPrice)
        } else {
            // Không giảm giá
            holder.dishOriginalPrice.visibility = View.GONE
            holder.dishDiscountedPrice.text = format.format(dish.originalPrice)
        }

        // Hiển thị lời khuyên sức khỏe
        if (dish.healthTips.isNotBlank()) {
            holder.dishHealthTips.text = dish.healthTips
            holder.dishHealthTips.visibility = View.VISIBLE
        } else {
            holder.dishHealthTips.visibility = View.GONE
        }

        Glide.with(context)
            .load(dish.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.dishImage)

        holder.itemView.setOnClickListener {
            onItemClick(dish)
        }
    }

    override fun getItemCount(): Int = dishes.size

    fun updateDishes(newDishes: List<Dish>) {
        this.dishes = newDishes
        notifyDataSetChanged()
    }
}