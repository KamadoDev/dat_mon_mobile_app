package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.doan_adr.smart_order_app.Models.Topping
import com.doan_adr.smart_order_app.databinding.DialogAddToppingBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.launch
import java.util.UUID

class AddToppingDialogFragment : DialogFragment() {

    private var _binding: DialogAddToppingBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseDatabaseManager()

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddToppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveTopping.setOnClickListener {
            val name = binding.etToppingName.text.toString().trim()
            val priceStr = binding.etToppingPrice.text.toString().trim()

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tên và giá topping.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null) {
                Toast.makeText(context, "Giá tiền không hợp lệ.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val newTopping = Topping(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        price = price
                    )
                    firebaseManager.addTopping(newTopping)
                    Toast.makeText(context, "Thêm topping thành công!", Toast.LENGTH_SHORT).show()
                    setFragmentResult(AddDishDialogFragment.RELOAD_TOPPINGS, Bundle.EMPTY)
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi khi thêm topping: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCancelTopping.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}