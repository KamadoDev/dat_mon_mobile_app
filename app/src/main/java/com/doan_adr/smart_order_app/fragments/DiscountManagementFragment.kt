package com.doan_adr.smart_order_app.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.Models.Discount
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.DiscountGridAdapter
import com.doan_adr.smart_order_app.databinding.FragmentDiscountManagementBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class DiscountManagementFragment : Fragment() {

    private var _binding: FragmentDiscountManagementBinding? = null
    private val binding get() = _binding!!
    private val calendar = Calendar.getInstance()
    private lateinit var discountAdapter: DiscountGridAdapter
    private var discountsListener: ListenerRegistration? = null
    private var firebaseManager = FirebaseDatabaseManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscountManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDiscounts()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val discounts = mutableListOf<Discount>()
        discountAdapter = DiscountGridAdapter(discounts) { discount ->
            // TODO: Xử lý sự kiện khi click vào một mã khuyến mãi (ví dụ: sửa, xóa)
            Log.d("DiscountManagementFragment", "Mã khuyến mãi ${discount.code} đã được click!")
        }
        binding.rvDiscounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = discountAdapter
        }
    }

    private fun loadDiscounts() {
        discountsListener = firebaseManager.getDiscounts(
            onSuccess = { discounts ->
                discountAdapter.updateData(discounts)
            },
            onError = { e ->
                Log.e("DiscountManagementFragment", "Lỗi khi tải dữ liệu khuyến mãi", e)
                Toast.makeText(context, "Lỗi khi tải dữ liệu khuyến mãi", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setupListeners() {
        binding.fabAddDiscount.setOnClickListener {
            showAddDiscountDialog()
        }
    }

    /**
     * Hiển thị dialog để thêm mã khuyến mãi mới.
     * Dialog chứa các trường nhập liệu tương ứng với model Discount.
     * Người dùng có thể nhập thông tin và lưu vào Firebase.
     */
    private fun showAddDiscountDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_discount, null)
        val codeEditText = dialogView.findViewById<EditText>(R.id.et_discount_code)
        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.rg_discount_type)
        val valueEditText = dialogView.findViewById<EditText>(R.id.et_discount_value)
        val minOrderEditText = dialogView.findViewById<EditText>(R.id.et_min_order_value)
        val maxDiscountEditText = dialogView.findViewById<EditText>(R.id.et_max_discount)
        val validUntilEditText = dialogView.findViewById<EditText>(R.id.et_valid_until)
        val usageLimitEditText = dialogView.findViewById<EditText>(R.id.et_usage_limit)

        // Mở DatePicker khi nhấn vào trường ngày hết hạn
        validUntilEditText.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                validUntilEditText.setText(dateFormat.format(calendar.time))
            }

            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm Mã Khuyến Mãi Mới")
            .setView(dialogView)
            .setPositiveButton("Thêm") { dialog, which ->
                // Lấy dữ liệu từ các trường
                val code = codeEditText.text.toString().trim()
                val discountType = if (typeRadioGroup.checkedRadioButtonId == R.id.rb_percentage) "percentage" else "fixed"
                val value = valueEditText.text.toString().toDoubleOrNull() ?: 0.0
                val minOrderValue = minOrderEditText.text.toString().toDoubleOrNull() ?: 0.0
                val maxDiscount = maxDiscountEditText.text.toString().toDoubleOrNull() ?: 0.0
                val validUntil = validUntilEditText.text.toString()
                val usageLimit = usageLimitEditText.text.toString().toIntOrNull() ?: 0

                // Kiểm tra validation
                if (code.isEmpty() || value == 0.0) {
                    Toast.makeText(context, "Mã và giá trị không được để trống.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Tạo đối tượng Discount
                val newDiscount = Discount(
                    code = code,
                    discountType = discountType,
                    value = value,
                    minOrderValue = minOrderValue,
                    maxDiscount = maxDiscount,
                    validUntil = validUntil,
                    usageLimit = usageLimit,
                    timesUsed = 0
                )

                // Gọi hàm thêm mã khuyến mãi
                firebaseManager.addDiscount(
                    discount = newDiscount,
                    onSuccess = {
                        Toast.makeText(context, "Thêm mã khuyến mãi thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "Thêm mã khuyến mãi thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        discountsListener?.remove()
        _binding = null
    }
}