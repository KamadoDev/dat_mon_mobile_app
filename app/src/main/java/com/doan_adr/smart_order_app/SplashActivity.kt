package com.doan_adr.smart_order_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.UnknownHostException

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val databaseManager = FirebaseDatabaseManager()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAppInitialization()
    }

    private fun startAppInitialization() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Vị trí để gọi hàm cập nhật
                updateOldUserAccounts()

                // Chờ hàm này hoàn thành
                //databaseManager.createMockData()
                //databaseManager.createMockAccounts()
                // Sau đó mới chuyển màn hình
                navigateToRoleSelection()
            } catch (e: Exception) {
                Log.e("SplashActivity", "Lỗi khi tạo dữ liệu mẫu: ${e.message}")
                // Phân tích lỗi cụ thể
                if (e.cause is UnknownHostException) {
                    showNetworkErrorDialog()
                } else {
                    navigateToRoleSelection()
                }
            }
        }
    }

    /**
     * Cập nhật tất cả các tài khoản người dùng cũ không có trường `isAccountEnabled`.
     * Hàm này sẽ gán giá trị mặc định là `true` cho các tài khoản này.
     * Nên chạy một lần duy nhất.
     */
    private suspend fun updateOldUserAccounts() {
        Log.d("SplashActivity", "Đang kiểm tra và cập nhật các tài khoản cũ...")
        try {
            val usersRef = db.collection("users")
            val querySnapshot = usersRef.get().await()

            val batch = db.batch()
            var usersUpdatedCount = 0

            for (document in querySnapshot.documents) {
                // Kiểm tra xem trường "isAccountEnabled" có tồn tại hay không
                if (!document.contains("isAccountEnabled")) {
                    // Nếu không tồn tại, thêm trường này với giá trị true
                    batch.update(document.reference, "isAccountEnabled", true)
                    usersUpdatedCount++
                    Log.d("SplashActivity", "Đã đánh dấu cập nhật tài khoản: ${document.id}")
                }
            }

            if (usersUpdatedCount > 0) {
                batch.commit().await()
                Log.d("SplashActivity", "Đã cập nhật thành công $usersUpdatedCount tài khoản.")
            } else {
                Log.d("SplashActivity", "Không tìm thấy tài khoản cũ nào cần cập nhật.")
            }
        } catch (e: Exception) {
            Log.e("SplashActivity", "Lỗi khi cập nhật tài khoản cũ: ${e.message}", e)
        }
    }

    private fun navigateToRoleSelection() {
        val intent = Intent(this, RoleSelectionActivity::class.java)
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
