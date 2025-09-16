package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope // Sử dụng lifecycleScope thay cho CoroutineScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Table
import com.doan_adr.smart_order_app.adapters.TableAdapter
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class TableSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tableAdapter: TableAdapter
    private val databaseManager = FirebaseDatabaseManager()
    private var tablesListenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_selection)

        recyclerView = findViewById(R.id.tables_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        tableAdapter = TableAdapter(this, emptyList()) { table ->
            onTableSelected(table)
        }
        recyclerView.adapter = tableAdapter

        // Chỉ cần gọi hàm lắng nghe để vừa tải ban đầu, vừa cập nhật theo thời gian thực
        tablesListenerRegistration = databaseManager.addTablesListener { tables ->
            tableAdapter.updateTables(tables)
        }
    }

    private fun onTableSelected(table: Table) {
        if (table.status == "available") {
            Toast.makeText(this@TableSelectionActivity, "Đang khóa bàn...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                try {
                    // Khóa bàn ngay lập tức và chờ phản hồi
                    databaseManager.lockTable(table.id)

                    // Chuyển màn hình sau khi bàn đã được khóa thành công
                    val intent = Intent(this@TableSelectionActivity, MenuActivity::class.java).apply {
                        putExtra("tableId", table.id)
                        putExtra("tableName", table.name)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("TableSelectionActivity", "Lỗi khi khóa bàn: ${e.message}")
                    Toast.makeText(this@TableSelectionActivity, "Có lỗi xảy ra, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@TableSelectionActivity, "Bàn này đã có khách, vui lòng chọn bàn khác.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tablesListenerRegistration?.remove()
    }
}