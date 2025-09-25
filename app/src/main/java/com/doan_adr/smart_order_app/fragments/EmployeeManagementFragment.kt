package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.adapters.EmployeeManagementAdapter
import com.doan_adr.smart_order_app.databinding.FragmentEmployeeManagementBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.doan_adr.smart_order_app.viewmodels.EmployeeEvent
import com.doan_adr.smart_order_app.viewmodels.SharedEmployeeViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class EmployeeManagementFragment : Fragment() {

    private var _binding: FragmentEmployeeManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var employeeAdapter: EmployeeManagementAdapter
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null

    // Lấy instance của Shared ViewModel
    private val sharedViewModel: SharedEmployeeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        listenToEmployeeChanges()
        listenToEmployeeResult()
    }

    private fun listenToEmployeeResult() {
        Log.d("EmployeeManagementFragment", "Bắt đầu lắng nghe LiveData từ Shared ViewModel.")
        sharedViewModel.employeeEvent.observe(viewLifecycleOwner) { event ->
            event?.let { employeeEvent ->
                lifecycleScope.launch {
                    try {
                        when (employeeEvent) {
                            is EmployeeEvent.AddEmployee -> {
                                // Chế độ thêm mới
                                Log.d("EmployeeManagementFragment", "Đã nhận sự kiện thêm nhân viên. ID: ${employeeEvent.user.uid}")
                                Log.d("EmployeeManagementFragment", "Giá trị mật khẩu: ${employeeEvent.password}")
                                if (employeeEvent.password.isNotEmpty()) {
                                    Log.d("EmployeeManagementFragment", "Gọi hàm addUserWithAuth...")
                                    firebaseManager.addUserWithAuth(employeeEvent.user, employeeEvent.password)
                                    Log.d("EmployeeManagementFragment", "addUserWithAuth đã hoàn thành.")
                                    Toast.makeText(context, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
                                    Log.i("EmployeeManagementFragment", "Đã thêm nhân viên ${employeeEvent.user.username} thành công.")
                                } else {
                                    Toast.makeText(context, "Mật khẩu không được để trống.", Toast.LENGTH_SHORT).show()
                                    Log.w("EmployeeManagementFragment", "Mật khẩu rỗng, không thể thêm nhân viên mới.")
                                }
                            }
                            is EmployeeEvent.UpdateEmployee -> {
                                // Chế độ cập nhật
                                Log.d("EmployeeManagementFragment", "Đã nhận sự kiện cập nhật nhân viên. ID: ${employeeEvent.user.uid}")
                                Log.d("EmployeeManagementFragment", "Gọi hàm updateUser...")
                                firebaseManager.updateUser(employeeEvent.user)
                                Log.d("EmployeeManagementFragment", "updateUser đã hoàn thành.")
                                Toast.makeText(context, "Cập nhật nhân viên thành công!", Toast.LENGTH_SHORT).show()
                                Log.i("EmployeeManagementFragment", "Đã cập nhật nhân viên ${employeeEvent.user.username} thành công.")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("EmployeeManagementFragment", "Lỗi khi lưu nhân viên: ${e.message}", e)
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        sharedViewModel.clearEvent() // Xóa sự kiện sau khi xử lý
                        Log.d("EmployeeManagementFragment", "Đã xóa sự kiện trong ViewModel.")
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeManagementAdapter(
            mutableListOf(),
            onEditClick = { employee ->
                val dialog = AddEmployeeDialogFragment.newInstance(employee)
                dialog.show(childFragmentManager, "AddEmployeeDialogFragment")
            }
        )
        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = employeeAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddEmployee.setOnClickListener {
            Log.d("EmployeeManagementFragment", "Nút 'Thêm nhân viên' đã được nhấn.")
            val dialog = AddEmployeeDialogFragment()
            dialog.show(childFragmentManager, "AddEmployeeDialogFragment")
        }
    }

    private fun listenToEmployeeChanges() {
        listenerRegistration = firebaseManager.getUsersByRole("manager", "chef")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("EmployeeManagementFragment", "Listen failed.", e)
                    Toast.makeText(context, "Lỗi khi lấy dữ liệu nhân viên.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshots != null && !snapshots.isEmpty) {
                    val employees = snapshots.toObjects(User::class.java)
                    employeeAdapter.updateData(employees)
                } else {
                    Log.d("EmployeeManagementFragment", "Current data: null")
                    employeeAdapter.updateData(emptyList())
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}
