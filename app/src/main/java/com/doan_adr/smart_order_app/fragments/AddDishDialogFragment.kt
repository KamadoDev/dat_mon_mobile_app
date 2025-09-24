package com.doan_adr.smart_order_app.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.adapters.ToppingManagementAdapter
import com.doan_adr.smart_order_app.databinding.DialogAddDishBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.bumptech.glide.Glide // Thêm import cho Glide
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Suppress("DEPRECATION")
class AddDishDialogFragment : DialogFragment() {
    // Khai báo Shared ViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var dishToEdit: Dish? = null
    private var _binding: DialogAddDishBinding? = null
    private val binding get() = _binding!!
    private val firebaseManager = FirebaseDatabaseManager()

    private var categories: List<Category> = emptyList()
    private var selectedCategory: Category? = null

    private lateinit var toppingAdapter: ToppingManagementAdapter

    companion object {
        const val RELOAD_CATEGORIES = "reload_categories_key"
        const val RELOAD_TOPPINGS = "reload_toppings_key"
        fun newInstance(dish: Dish): AddDishDialogFragment {
            val args = Bundle()
            args.putParcelable("dish_to_edit", dish)
            val fragment = AddDishDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy đối tượng Dish nếu có
        arguments?.let {
            dishToEdit = it.getParcelable("dish_to_edit")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupToppingRecyclerView()
        setupFragmentResultListeners()
        loadData()
        // Điền sẵn dữ liệu nếu đang ở chế độ chỉnh sửa
        if (dishToEdit != null) {
            fillDataForEditing(dishToEdit!!)
        }
    }

    private fun fillDataForEditing(dish: Dish) {
        binding.tvDialogTitle.text = "Chỉnh Sửa Món Ăn"
        binding.btnAddDish.text = "Lưu"

        binding.etDishName.setText(dish.name)
        binding.etDishDescription.setText(dish.description)
        binding.etOriginalPrice.setText(dish.originalPrice.toString())
        binding.etDiscountedPrice.setText(dish.discountedPrice.toString())
        binding.etHealthTips.setText(dish.healthTips)
        binding.etDishImageUrl.setText(dish.imageUrl)

        // Hiển thị ảnh xem trước
        Glide.with(this).load(dish.imageUrl).into(binding.ivDishImagePreview)

        // Chọn danh mục tương ứng
        lifecycleScope.launch {
            selectedCategory = categories.find { it.id == dish.categoryId }
            selectedCategory?.let {
                binding.actvCategory.setText(it.name, false)
            }
        }
        // Chọn các topping đã có
        toppingAdapter.updateSelectedToppings(dish.toppingIds.toMutableSet())
    }

    private fun setupToppingRecyclerView() {
        toppingAdapter = ToppingManagementAdapter(mutableListOf()) {
            // Callback khi chọn/bỏ chọn topping
        }
        binding.rvToppings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = toppingAdapter
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                categories = firebaseManager.getCategories()
                val categoryNames = categories.map { it.name }
                val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames)
                binding.actvCategory.setAdapter(categoryAdapter)

                val toppings = firebaseManager.getToppings()
                toppingAdapter.updateData(toppings)
            } catch (e: Exception) {
                Log.e("AddDishDialog", "Lỗi khi tải dữ liệu: ${e.message}", e)
                Toast.makeText(context, "Lỗi khi tải dữ liệu. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.actvCategory.setOnItemClickListener { parent, view, position, id ->
            selectedCategory = categories[position]
        }

        binding.btnAddCategory.setOnClickListener {
            val dialog = AddCategoryDialogFragment()
            dialog.show(childFragmentManager, "AddCategoryDialogFragment")
        }

        binding.btnAddTopping.setOnClickListener {
            val dialog = AddToppingDialogFragment()
            dialog.show(childFragmentManager, "AddToppingDialogFragment")
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAddDish.setOnClickListener {
            validateAndAddDish()
        }

        // Thêm TextWatcher để xem trước ảnh
        binding.etDishImageUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val imageUrl = s.toString().trim()
                if (imageUrl.isNotEmpty()) {
                    // Sử dụng Glide để tải và hiển thị ảnh
                    Glide.with(this@AddDishDialogFragment)
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(R.drawable.image_24px) // Bạn có thể thêm một ảnh placeholder
                        .error(R.drawable.broken_image_24px) // Thêm ảnh hiển thị khi lỗi
                        .into(binding.ivDishImagePreview)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAndAddDish() {
        val name = binding.etDishName.text.toString().trim()
        val description = binding.etDishDescription.text.toString().trim()
        val originalPriceStr = binding.etOriginalPrice.text.toString().trim()
        val discountedPriceStr = binding.etDiscountedPrice.text.toString().trim()
        val healthTips = binding.etHealthTips.text.toString().trim()
        val imageUrl = binding.etDishImageUrl.text.toString().trim()

        val selectedToppingIds = toppingAdapter.getSelectedToppingIds()
        // Thêm dòng log này để kiểm tra danh sách ID topping
        Log.d("AddDishDialog", "Selected Topping IDs: $selectedToppingIds")

        if (name.isEmpty() || originalPriceStr.isEmpty() || selectedCategory == null || imageUrl.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin bắt buộc và nhập URL ảnh.", Toast.LENGTH_SHORT).show()
            return
        }

        val originalPrice = originalPriceStr.toDoubleOrNull()
        if (originalPrice == null || originalPrice < 0) {
            Toast.makeText(context, "Giá gốc không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        val discountedPrice = discountedPriceStr.toDoubleOrNull()
        if (discountedPrice != null && discountedPrice > originalPrice) {
            Toast.makeText(context, "Giá giảm không được lớn hơn giá gốc.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val newDish = Dish(
                    id = dishToEdit?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    imageUrl = imageUrl,
                    originalPrice = originalPrice,
                    discountedPrice = discountedPrice ?: 0.0,
                    categoryId = selectedCategory!!.id,
                    healthTips = healthTips,
                    toppingIds = selectedToppingIds,
                    isAvailable = true,
                    toppingsAvailable = selectedToppingIds.isNotEmpty()
                )

//                val resultBundle = bundleOf("new_dish" to newDish)
//                setFragmentResult("add_dish_request", resultBundle)
                // Gửi đối tượng Dish tới Shared ViewModel thay vì sử dụng Fragment Result
                sharedViewModel.setNewDish(newDish)

                dismiss()
            } catch (e: Exception) {
                Log.e("AddDishDialog", "Lỗi khi thêm món ăn: ${e.message}", e)
                Toast.makeText(context, "Lỗi: Không thể thêm món ăn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFragmentResultListeners() {
        setFragmentResultListener(RELOAD_CATEGORIES) { _, _ ->
            loadData()
        }

        setFragmentResultListener(RELOAD_TOPPINGS) { _, _ ->
            loadData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}