package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.databinding.ActivityLoginBinding
import com.doan_adr.smart_order_app.utils.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authManager = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRole = intent.getStringExtra("user_role")

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val user = authManager.login(username, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // Kiểm tra vai trò và chuyển hướng
                        val requiredRole = intent.getStringExtra("user_role")
                        if (user.role == requiredRole) {
                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard(user.role)
                        } else {
                            Toast.makeText(this@LoginActivity, "Vai trò không hợp lệ", Toast.LENGTH_SHORT).show()
                            authManager.signOut() // Đăng xuất nếu sai vai trò
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToDashboard(role: String?) {
        val intent: Intent
        when (role) {
            "chef" -> {
                intent = Intent(this, ChefDashboardActivity::class.java)
            }
            "manager" -> {
                intent = Intent(this, ManagerDashboardActivity::class.java)
            }
            else -> {
                // Should not happen, but as a fallback
                return
            }
        }
        startActivity(intent)
        finish()
    }
}