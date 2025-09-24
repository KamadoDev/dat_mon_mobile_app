package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.databinding.ItemDishManagementBinding
import java.text.NumberFormat
import java.util.Locale

class DishManagementAdapter(
    private val dishes: MutableList<Dish>,
    private val onEditClick: (Dish) -> Unit,
    private val onDeleteClick: (Dish) -> Unit
) : RecyclerView.Adapter<DishManagementAdapter.DishViewHolder>() {

    class DishViewHolder(val binding: ItemDishManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        val binding = ItemDishManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DishViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val dish = dishes[position]
        holder.binding.apply {
            dishName.text = dish.name
            dishDescription.text = dish.description

            Glide.with(root.context)
                .load(dish.imageUrl)
                .placeholder(R.drawable.pho_bo)
                .error(R.drawable.pho_bo)
                .into(dishImage)

            val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            dishOriginalPrice.text = "${format.format(dish.originalPrice)}đ"
            dishDiscountedPrice.text = "${format.format(dish.discountedPrice)}đ"

            btnEdit.setOnClickListener { onEditClick(dish) }
            btnDelete.setOnClickListener { onDeleteClick(dish) }
        }
    }

    override fun getItemCount() = dishes.size

    fun updateData(newDishes: List<Dish>) {
        dishes.clear()
        dishes.addAll(newDishes)
        notifyDataSetChanged()
    }
}