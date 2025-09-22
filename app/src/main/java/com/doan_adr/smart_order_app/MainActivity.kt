package com.doan_adr.smart_order_app

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore
    // Khởi tạo FirebaseDatabaseManager
    private val databaseManager = FirebaseDatabaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Gọi hàm tạo dữ liệu mẫu
        createMockData()
    }

    private fun createMockData() {
        // Sử dụng coroutine scope để gọi suspend function
        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseManager.createMockData()
            } catch (e: Exception) {
                Log.e("MainActivity", "Lỗi khi tạo dữ liệu mẫu: ${e.message}")
            }
        }
    }
}