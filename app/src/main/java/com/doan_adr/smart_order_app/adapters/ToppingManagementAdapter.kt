// File: adapters/ToppingAdapter.kt

package com.doan_adr.smart_order_app.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Topping
import com.doan_adr.smart_order_app.databinding.ItemToppingCheckboxBinding
import java.text.NumberFormat
import java.util.Locale

class ToppingManagementAdapter(
    private val toppings: MutableList<Topping>,
    private val onToppingSelected: (Topping) -> Unit
) : RecyclerView.Adapter<ToppingManagementAdapter.ToppingViewHolder>() {

    private val selectedToppingIds: MutableSet<String> = mutableSetOf()

    class ToppingViewHolder(val binding: ItemToppingCheckboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToppingViewHolder {
        val binding = ItemToppingCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ToppingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToppingViewHolder, position: Int) {
        val topping = toppings[position]
        holder.binding.apply {
            tvToppingName.text = topping.name
            val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
            tvToppingPrice.text = "${format.format(topping.price)}đ"

            cbToppingSelect.isChecked = selectedToppingIds.contains(topping.id)

            cbToppingSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedToppingIds.add(topping.id)
                } else {
                    selectedToppingIds.remove(topping.id)
                }
                onToppingSelected(topping)
            }
        }
    }

    override fun getItemCount() = toppings.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newToppings: List<Topping>) {
        toppings.clear()
        toppings.addAll(newToppings)
        selectedToppingIds.clear()
        notifyDataSetChanged()
    }

    /**
     * Cập nhật danh sách các topping đã được chọn.
     * Thường được gọi khi ở chế độ chỉnh sửa món ăn.
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateSelectedToppings(ids: MutableSet<String>) {
        selectedToppingIds.clear()
        selectedToppingIds.addAll(ids)
        notifyDataSetChanged()
    }

    fun getSelectedToppingIds(): List<String> {
        return selectedToppingIds.toList()
    }
}