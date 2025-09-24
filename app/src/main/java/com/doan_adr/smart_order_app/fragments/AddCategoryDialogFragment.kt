package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.databinding.DialogAddCategoryBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.launch
import java.util.UUID

class AddCategoryDialogFragment : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseDatabaseManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveCategory.setOnClickListener {
            val categoryName = binding.etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên danh mục.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val newCategory = Category(
                        id = UUID.randomUUID().toString(),
                        name = categoryName
                    )
                    firebaseManager.addCategory(newCategory)
                    Toast.makeText(context, "Thêm danh mục thành công!", Toast.LENGTH_SHORT).show()
                    setFragmentResult(AddDishDialogFragment.RELOAD_CATEGORIES, Bundle.EMPTY)
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi khi thêm danh mục: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCancelCategory.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}