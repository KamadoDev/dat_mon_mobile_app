package com.doan_adr.smart_order_app.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.databinding.ItemCategoryRadioBinding

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedCategoryId: String? = null

    class CategoryViewHolder(val binding: ItemCategoryRadioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryRadioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        Log.d("CategoryAdapter", "Đang binding danh mục: ${category.name} tại vị trí $position")
        holder.binding.apply {
            tvCategoryName.text = category.name
            rbCategorySelect.isChecked = category.id == selectedCategoryId

            root.setOnClickListener {
                if (category.id != selectedCategoryId) {
                    selectedCategoryId = category.id
                    notifyDataSetChanged()
                    onCategorySelected(category)
                }
            }

            rbCategorySelect.setOnClickListener {
                if (category.id != selectedCategoryId) {
                    selectedCategoryId = category.id
                    notifyDataSetChanged()
                    onCategorySelected(category)
                }
            }
        }
    }

    override fun getItemCount() = categories.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)

        // Kiểm tra nếu danh sách mới không rỗng và chưa có danh mục nào được chọn
        // thì chọn mặc định danh mục đầu tiên.
        if (newCategories.isNotEmpty() && selectedCategoryId == null) {
            selectedCategoryId = newCategories.first().id
        }

        notifyDataSetChanged()

        // Gọi callback cho danh mục đã chọn sau khi cập nhật dữ liệu
        selectedCategoryId?.let { id ->
            categories.find { it.id == id }?.let { onCategorySelected(it) }
        }
    }

    fun getSelectedCategory(): Category? {
        return categories.find { it.id == selectedCategoryId }
    }
}