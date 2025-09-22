package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import android.view.View
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

            // Bắt đầu hiển thị loading
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false // Vô hiệu hóa nút để tránh click lặp lại
            binding.etUsername.isEnabled = false
            binding.etPassword.isEnabled = false

            CoroutineScope(Dispatchers.IO).launch {
                val user = authManager.login(username, password)
                withContext(Dispatchers.Main) {
                    // Dừng hiển thị loading và bật lại các thành phần UI
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.etUsername.isEnabled = true
                    binding.etPassword.isEnabled = true

                    if (user != null) {
                        val requiredRole = intent.getStringExtra("user_role")
                        if (user.role == requiredRole) {
                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard(user.role)
                        } else {
                            val roleMessage = if (requiredRole == "chef") "đầu bếp" else "quản lý"
                            Toast.makeText(this@LoginActivity, "Tên đăng nhập này không phải là $roleMessage", Toast.LENGTH_LONG).show()
                            authManager.signOut()
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