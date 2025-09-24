package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.adapters.EmployeeManagementAdapter
import com.doan_adr.smart_order_app.databinding.FragmentEmployeeManagementBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration

class EmployeeManagementFragment : Fragment() {

    private var _binding: FragmentEmployeeManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var employeeAdapter: EmployeeManagementAdapter
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null

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
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeManagementAdapter(
            mutableListOf(),
            onEditClick = { employee ->
                Log.d("EmployeeManagement", "Edit employee: ${employee.username}")
                // TODO: Xử lý logic chỉnh sửa nhân viên
            },
            onDeleteClick = { employee ->
                Log.d("EmployeeManagement", "Delete employee: ${employee.username}")
                // TODO: Xử lý logic xóa nhân viên
            }
        )

        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = employeeAdapter
        }
    }

    private fun setupListeners() {
        binding.fabAddEmployee.setOnClickListener {
            Log.d("EmployeeManagement", "Add new employee")
            // TODO: Xử lý logic thêm nhân viên mới
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