// File: fragments/DishManagementFragment.kt

package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.adapters.DishManagementAdapter
import com.doan_adr.smart_order_app.databinding.FragmentDishManagementBinding
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.doan_adr.smart_order_app.viewmodels.SharedViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class DishManagementFragment : Fragment() {
    // Khai báo Shared ViewModel
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentDishManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var dishAdapter: DishManagementAdapter
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        listenToDishChanges()

        // Thêm observer để lắng nghe thay đổi từ Shared ViewModel
        sharedViewModel.newDish.observe(viewLifecycleOwner) { dish ->
            dish?.let {
                lifecycleScope.launch {
                    try {
                        firebaseManager.addDish(it)
                        Toast.makeText(context, "Thêm món ăn thành công!", Toast.LENGTH_SHORT).show()
                        sharedViewModel.clearNewDish() // Xóa dữ liệu sau khi thêm thành công
                    } catch (e: Exception) {
                        Log.e("DishManagementFragment", "Lỗi khi thêm món ăn vào Firestore: ${e.message}", e)
                        Toast.makeText(context, "Lỗi: Không thể thêm món ăn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    private fun setupRecyclerView() {
        dishAdapter = DishManagementAdapter(
            dishes = mutableListOf(),
            onEditClick = { dish ->
                // TODO: Xử lý sự kiện chỉnh sửa món ăn
                val dialog = AddDishDialogFragment.newInstance(dish)
                dialog.show(childFragmentManager, "AddDishDialogFragment")
                Toast.makeText(context, "Chỉnh sửa ${dish.name}.", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { dish ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa món ăn ${dish.name}?")
                    .setNegativeButton("Hủy") { dialog, which ->
                        // Không làm gì cả, đóng dialog
                    }
                    .setPositiveButton("Xóa") { dialog, which ->
                        // Xử lý việc xóa món ăn
                        deleteDish(dish)
                    }
                    .show()
            }
        )
        binding.dishList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dishAdapter
        }
    }

    // Thêm hàm deleteDish
    private fun deleteDish(dish: Dish) {
        lifecycleScope.launch {
            try {
                firebaseManager.deleteDish(dish.id)
                Toast.makeText(context, "Đã xóa ${dish.name} thành công!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("DishManagementFragment", "Lỗi khi xóa món ăn: ${e.message}", e)
                Toast.makeText(context, "Lỗi: Không thể xóa món ăn. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.btnAddDish.setOnClickListener {
            val dialog = AddDishDialogFragment()
            dialog.show(childFragmentManager, "AddDishDialogFragment")
        }
    }

    private fun listenToDishChanges() {
        // Lắng nghe thay đổi từ Firestore để cập nhật danh sách món ăn theo thời gian thực
        listenerRegistration = firebaseManager.addDishesListener { dishes ->
            dishAdapter.updateData(dishes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}