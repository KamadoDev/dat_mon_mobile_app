package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Topping
import com.doan_adr.smart_order_app.R
import java.text.NumberFormat
import java.util.Locale

class ToppingAdapter(
    private var toppings: List<Topping>,
    private val onToppingStateChanged: (Topping, Boolean) -> Unit
) : RecyclerView.Adapter<ToppingAdapter.ToppingViewHolder>() {

    class ToppingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val toppingCheckbox: CheckBox = view.findViewById(R.id.topping_checkbox)
        val toppingName: TextView = view.findViewById(R.id.topping_name)
        val toppingPrice: TextView = view.findViewById(R.id.topping_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToppingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_topping_option, parent, false)
        return ToppingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ToppingViewHolder, position: Int) {
        val topping = toppings[position]

        // Thiết lập tên topping
        holder.toppingName.text = topping.name

        // Định dạng và hiển thị giá, chỉ khi giá lớn hơn 0
        if (topping.price > 0) {
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(topping.price)
            holder.toppingPrice.text = "+ $formattedPrice"
            holder.toppingPrice.visibility = View.VISIBLE
        } else {
            // Ẩn TextView giá nếu topping miễn phí
            holder.toppingPrice.visibility = View.GONE
        }

        // Đặt trạng thái ban đầu là chưa được chọn
        holder.toppingCheckbox.isChecked = false

        // ✨ THÊM ĐOẠN CODE NÀY ĐỂ LẮNG NGHE SỰ KIỆN CHECKBOX ✨
        // onCheckedChangeListener sẽ được gọi khi trạng thái của CheckBox thay đổi.
        holder.toppingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onToppingStateChanged(topping, isChecked)
        }

        // ✨ THÊM ĐOẠN CODE NÀY ĐỂ KHI CLICK CẢ ITEM VẪN HOẠT ĐỘNG ✨
        // itemClickListener sẽ đảo ngược trạng thái của CheckBox khi người dùng nhấn vào bất cứ đâu trên item.
        holder.itemView.setOnClickListener {
            holder.toppingCheckbox.isChecked = !holder.toppingCheckbox.isChecked
        }
    }

    override fun getItemCount(): Int = toppings.size

    fun updateToppings(newToppings: List<Topping>) {
        this.toppings = newToppings
        notifyDataSetChanged()
    }
}