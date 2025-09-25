// File: StatisticsFragment.kt
package com.doan_adr.smart_order_app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doan_adr.smart_order_app.Models.Report
import com.doan_adr.smart_order_app.adapters.DailyReportAdapter
import com.doan_adr.smart_order_app.databinding.FragmentStatisticsBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseDatabaseManager()

    private lateinit var dailyReportAdapter: DailyReportAdapter
    private val dailyReports = mutableListOf<Report>()

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadOverallStats()
        setupListeners()
        loadDailyReports()
    }

    private fun setupUI() {
        dailyReportAdapter = DailyReportAdapter(dailyReports)
        binding.rvDailyReports.adapter = dailyReportAdapter
    }

    private fun loadOverallStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val overallStats = firebaseManager.getOverallStats()
                val format = java.text.NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                binding.tvOverallRevenue.text = format.format(overallStats["totalRevenue"] ?: 0.0)
                binding.tvOverallOrders.text = (overallStats["totalOrders"] ?: 0L).toString()
            } catch (e: Exception) {
                Log.e("StatisticsFragment", "Lỗi tải số liệu tổng quan: ${e.message}")
                Toast.makeText(context, "Lỗi khi tải số liệu tổng quan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelectStartDate.setOnClickListener {
            showDatePicker(true)
        }

        binding.btnSelectEndDate.setOnClickListener {
            showDatePicker(false)
        }

        binding.btnFilter.setOnClickListener {
            if (startDate != null && endDate != null) {
                loadDailyReports(startDate, endDate)
            } else {
                Toast.makeText(context, "Vui lòng chọn cả ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val selectedDate = calendar.time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate)

            if (isStartDate) {
                startDate = selectedDate
                binding.btnSelectStartDate.text = formattedDate
            } else {
                endDate = selectedDate
                binding.btnSelectEndDate.text = formattedDate
            }
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun loadDailyReports(start: Date? = null, end: Date? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val reports = firebaseManager.getDailyReports(start, end)
                dailyReports.clear()
                dailyReports.addAll(reports)
                dailyReportAdapter.notifyDataSetChanged()

                if (reports.isEmpty()) {
                    binding.tvEmptyReports.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyReports.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("StatisticsFragment", "Lỗi tải báo cáo: ${e.message}")
                Toast.makeText(context, "Lỗi khi tải báo cáo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}