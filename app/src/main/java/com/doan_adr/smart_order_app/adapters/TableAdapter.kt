package com.doan_adr.smart_order_app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Table
import com.doan_adr.smart_order_app.R
import com.google.android.material.card.MaterialCardView // Import MaterialCardView

class TableAdapter(
    private val context: Context,
    private var tables: List<Table>,
    private val onTableClicked: (Table) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.cardView) // Lấy reference đến cardView
        val tableName: TextView = itemView.findViewById(R.id.table_name)
        val statusIcon: ImageView = itemView.findViewById(R.id.status_icon)
        val orderIdText: TextView = itemView.findViewById(R.id.order_id_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_table, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        val table = tables[position]
        holder.tableName.text = table.name

        // Logic cập nhật giao diện dựa trên trạng thái
        if (table.status == "available") {
            // Cập nhật giao diện cho bàn trống
            holder.statusIcon.setImageResource(R.drawable.dine_lamp_24px)
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.green))
            holder.tableName.setTextColor(ContextCompat.getColor(context, R.color.dark))
            holder.orderIdText.text = "Trống"
            holder.orderIdText.setTextColor(ContextCompat.getColor(context, R.color.green))
            holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.green)
            holder.cardView.strokeWidth = 2
        } else {
            // Cập nhật giao diện cho bàn đã có khách
            holder.statusIcon.setImageResource(R.drawable.restaurant_24px)
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
            holder.tableName.setTextColor(ContextCompat.getColor(context, R.color.dark))
            holder.orderIdText.text = "Đơn: ${table.currentOrderId ?: "Đang chờ"}"
            holder.orderIdText.setTextColor(ContextCompat.getColor(context, R.color.gray))
            holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.primary)
            holder.cardView.strokeWidth = 2
        }

        holder.itemView.setOnClickListener {
            onTableClicked(table)
        }
    }

    override fun getItemCount(): Int = tables.size

    fun updateTables(newTables: List<Table>) {
        tables = newTables
        notifyDataSetChanged()
    }
}