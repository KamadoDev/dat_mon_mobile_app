package com.doan_adr.smart_order_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.databinding.ItemEmployeeManagementBinding

class EmployeeManagementAdapter(
    private val employees: MutableList<User>,
    private val onEditClick: (User) -> Unit
) : RecyclerView.Adapter<EmployeeManagementAdapter.EmployeeViewHolder>() {

    class EmployeeViewHolder(val binding: ItemEmployeeManagementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeManagementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]
        holder.binding.apply {
            // Tải ảnh đại diện bằng Glide
            Glide.with(root.context)
                .load(employee.avatar)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.person_24px)
                .error(R.drawable.person_24px)
                .into(ivEmployeeAvatar)

            // Hiển thị tên và vai trò
            tvEmployeeName.text = employee.username
            tvEmployeeRole.text = "Vai trò: ${getRoleText(employee.role)}"

            // Thiết lập sự kiện click cho nút chỉnh sửa
            btnEdit.setOnClickListener { onEditClick(employee) }
        }
    }

    override fun getItemCount() = employees.size

    fun updateData(newEmployees: List<User>) {
        employees.clear()
        employees.addAll(newEmployees)
        notifyDataSetChanged()
    }

    private fun getRoleText(role: String): String {
        return when (role) {
            "chef" -> "Đầu bếp"
            "manager" -> "Quản lý"
            else -> "Không xác định"
        }
    }
}
