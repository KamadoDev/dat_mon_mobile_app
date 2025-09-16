package com.doan_adr.smart_order_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class SplashActivity : AppCompatActivity() {

    private val databaseManager = FirebaseDatabaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAppInitialization()
    }

    private fun startAppInitialization() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chờ hàm này hoàn thành
                // databaseManager.createMockData()
                // Sau đó mới chuyển màn hình
                navigateToTableSelection()
            } catch (e: Exception) {
                Log.e("SplashActivity", "Lỗi khi tạo dữ liệu mẫu: ${e.message}")
                // Phân tích lỗi cụ thể
                if (e.cause is UnknownHostException) {
                    showNetworkErrorDialog()
                } else {
                    // Nếu là lỗi khác, vẫn chuyển màn hình hoặc xử lý khác
                    navigateToTableSelection()
                }
            }
        }
    }

    private fun navigateToTableSelection() {
        val intent = Intent(this, TableSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showNetworkErrorDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Lỗi kết nối")
                .setMessage("Vui lòng kiểm tra kết nối mạng của bạn và thử lại.")
                .setPositiveButton("Thử lại") { dialog, _ ->
                    dialog.dismiss()
                    startAppInitialization() // Tái khởi động quá trình
                }
                .setNegativeButton("Thoát") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }
}