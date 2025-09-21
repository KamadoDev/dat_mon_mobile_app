package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners for each button
        binding.btnCustomer.setOnClickListener {
            Log.d("RoleSelectionActivity", "Chuyển đến màn hình khách hàng")
            // Khách hàng sẽ được chuyển đến màn hình chọn bàn
            val intent = Intent(this, TableSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnChef.setOnClickListener {
            Log.d("RoleSelectionActivity", "Chuyển đến màn hình đầu bếp")
            // Đầu bếp cần đăng nhập trước, chuyển đến màn hình đăng nhập
            // Sau khi đăng nhập thành công, sẽ vào ChefDashboardActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("user_role", "chef")
            startActivity(intent)
            // finish() // Có thể giữ lại activity để quay lại
        }

        binding.btnManager.setOnClickListener {
            Log.d("RoleSelectionActivity", "Chuyển đến màn hình quản lý")
            // Tương tự, quản lý cũng cần đăng nhập
            // Sau khi đăng nhập thành công, sẽ vào ManagerDashboardActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("user_role", "manager")
            startActivity(intent)
            // finish() // Có thể giữ lại activity để quay lại
        }
    }
}