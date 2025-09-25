// File: adapters/DailyReportAdapter.kt
package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Report
import com.doan_adr.smart_order_app.databinding.ItemDailyReportBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DailyReportAdapter(
    private val reports: List<Report>
) : RecyclerView.Adapter<DailyReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemDailyReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemDailyReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.binding.apply {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvReportDate.text = "Ngày: ${dateFormat.format(report.date.toDate())}"

            val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            tvReportRevenue.text = "Doanh thu: ${format.format(report.revenue)}"

            tvReportOrderCount.text = "Số đơn hàng: ${report.orderCount}"
        }
    }

    override fun getItemCount() = reports.size
}