package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Discount
import com.doan_adr.smart_order_app.databinding.ItemDiscountCardBinding

class DiscountGridAdapter(
    private val discounts: MutableList<Discount>,
    private val onDiscountClick: (Discount) -> Unit
) : RecyclerView.Adapter<DiscountGridAdapter.DiscountViewHolder>() {

    class DiscountViewHolder(val binding: ItemDiscountCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = ItemDiscountCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        val discount = discounts[position]
        holder.binding.apply {
            tvDiscountCode.text = discount.code

            val discountText = when (discount.discountType) {
                "percentage" -> "Giảm ${discount.value.toInt()}%"
                "fixed" -> "Giảm ${String.format("%,.0f", discount.value)}đ"
                else -> "Không xác định"
            }
            tvDiscountDetails.text = "$discountText | Đơn hàng tối thiểu: ${String.format("%,.0f", discount.minOrderValue)}đ"

            tvValidUntil.text = "Hạn sử dụng: ${discount.validUntil}"

            root.setOnClickListener {
                onDiscountClick(discount)
            }
        }
    }

    override fun getItemCount() = discounts.size

    fun updateData(newDiscounts: List<Discount>) {
        discounts.clear()
        discounts.addAll(newDiscounts)
        notifyDataSetChanged()
    }
}