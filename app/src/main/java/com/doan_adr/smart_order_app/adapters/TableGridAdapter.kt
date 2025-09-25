package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Table
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.databinding.ItemTableGridBinding

class TableGridAdapter(
    private val tables: MutableList<Table>,
    private val onTableClick: (Table) -> Unit
) : RecyclerView.Adapter<TableGridAdapter.TableViewHolder>() {

    class TableViewHolder(val binding: ItemTableGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val table = tables[position]
        holder.binding.apply {
            tvTableName.text = table.name

            // Hiển thị Order ID nếu có
            if (!table.currentOrderId.isNullOrEmpty()) {
                tvOrderId.text = "Mã đơn hàng: ${table.currentOrderId}"
                tvOrderId.visibility = View.VISIBLE
            } else {
                tvOrderId.visibility = View.GONE
            }

            // Cập nhật trạng thái và màu sắc
            val context = root.context
            when (table.status) {
                "available" -> {
                    tvTableStatus.text = "Trạng thái: Trống"
                    root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.green))
                    ivTableIcon.setImageResource(R.drawable.restaurant_24px)
                    ivTableIcon.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                "occupied" -> {
                    tvTableStatus.text = "Trạng thái: Đã đặt"
                    root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red))
                    ivTableIcon.setImageResource(R.drawable.restaurant_24px)
                    ivTableIcon.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
                else -> {
                    tvTableStatus.text = "Trạng thái: Không xác định"
                    root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.gray))
                    ivTableIcon.setImageResource(R.drawable.restaurant_24px)
                    ivTableIcon.setColorFilter(ContextCompat.getColor(context, R.color.white))
                }
            }

            root.setOnClickListener {
                onTableClick(table)
            }
        }
    }

    override fun getItemCount() = tables.size

    fun updateData(newTables: List<Table>) {
        tables.clear()
        tables.addAll(newTables.sortedBy { it.tableNumber })
        notifyDataSetChanged()
    }
}