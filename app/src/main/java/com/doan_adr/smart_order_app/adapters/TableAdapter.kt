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
import android.graphics.PorterDuff

class TableAdapter(
    private val context: Context,
    private var tables: List<Table>,
    private val onTableClicked: (Table) -> Unit
) : RecyclerView.Adapter<TableAdapter.TableViewHolder>() {

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        // Logic cập nhật màu sắc dựa trên trạng thái
        if (table.status == "available") {
            // Thay đổi màu sắc của icon thành màu success
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.success), PorterDuff.Mode.SRC_IN)
            holder.tableName.setTextColor(ContextCompat.getColor(context, R.color.success))
            holder.orderIdText.visibility = View.GONE
        } else {
            // Thay đổi màu sắc của icon thành màu danger
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, R.color.danger), PorterDuff.Mode.SRC_IN)
            holder.tableName.setTextColor(ContextCompat.getColor(context, R.color.danger))
            holder.orderIdText.visibility = View.VISIBLE
            holder.orderIdText.text = "Đơn: ${table.currentOrderId ?: "N/A"}"
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