package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.doan_adr.smart_order_app.Models.Table
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.TableGridAdapter
import com.doan_adr.smart_order_app.databinding.FragmentTableManagementBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration

class TableManagementFragment : Fragment() {

    private var _binding: FragmentTableManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var tableAdapter: TableGridAdapter
    private var tablesListener: ListenerRegistration? = null
    private val firebaseManager = FirebaseDatabaseManager()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTableManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadTables()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val tables = mutableListOf<Table>()
        tableAdapter = TableGridAdapter(tables) { table ->
            // TODO: Xử lý sự kiện khi click vào bàn
            Log.d("TableManagementFragment", "Bàn ${table.name} đã được click!")
        }
        binding.rvTables.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = tableAdapter
        }
    }

    private fun loadTables() {
        tablesListener = firebaseManager.getTables(
            onSuccess = { tables ->
                tableAdapter.updateData(tables)
            },
            onError = { e ->
                Log.e("TableManagementFragment", "Lỗi khi tải dữ liệu bàn", e)
                // TODO: Hiển thị thông báo lỗi cho người dùng
            }
        )
    }

    private fun setupListeners() {
        binding.fabAddTable.setOnClickListener {
            showAddTableDialog()
        }
    }

    private fun showAddTableDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_table, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.et_table_name)
        val numberEditText = dialogView.findViewById<EditText>(R.id.et_table_number)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Thêm Bàn Mới")
            .setView(dialogView)
            .setPositiveButton("Thêm") { dialog, which ->
                val tableName = nameEditText.text.toString().trim()
                val tableNumberString = numberEditText.text.toString().trim()

                if (tableName.isEmpty() || tableNumberString.isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val tableNumber = tableNumberString.toIntOrNull()
                if (tableNumber == null) {
                    Toast.makeText(context, "Số bàn không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newTable = Table(
                    name = tableName,
                    tableNumber = tableNumber,
                    status = "available"
                )

                firebaseManager.addTable(
                    table = newTable,
                    onSuccess = {
                        Toast.makeText(context, "Thêm bàn thành công!", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "Thêm bàn thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tablesListener?.remove()
        _binding = null
    }
}