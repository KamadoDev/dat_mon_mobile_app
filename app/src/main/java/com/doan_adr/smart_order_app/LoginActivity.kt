package com.doan_adr.smart_order_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.databinding.ActivityLoginBinding
import com.doan_adr.smart_order_app.utils.AuthManager
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authManager = AuthManager()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    // Định nghĩa khóa cho SharedPreferences
    companion object {
        private const val PREFS_NAME = "login_prefs"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_USERNAME = "username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Tải trạng thái và username từ SharedPreferences
        loadLoginState()
        checkExistingSession()

        val userRole = intent.getStringExtra("user_role")

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lưu trạng thái của CheckBox trước khi đăng nhập
            saveLoginState()

            showLoading(true)

            CoroutineScope(Dispatchers.IO).launch {
                val user = authManager.login(username, password)
                withContext(Dispatchers.Main) {
                    showLoading(false)

                    if (user != null) {
                        val requiredRole = intent.getStringExtra("user_role")
                        if (user.role == requiredRole) {
                            Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard(user.role, user)
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

    private fun loadLoginState() {
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        binding.cbRememberMe.isChecked = rememberMe
        if (rememberMe) {
            val savedUsername = sharedPreferences.getString(KEY_USERNAME, "")
            binding.etUsername.setText(savedUsername)
        }
    }

    private fun saveLoginState() {
        val rememberMe = binding.cbRememberMe.isChecked
        with(sharedPreferences.edit()) {
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            if (rememberMe) {
                putString(KEY_USERNAME, binding.etUsername.text.toString().trim())
            } else {
                remove(KEY_USERNAME)
            }
            apply()
        }
    }

    private fun checkExistingSession() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            CoroutineScope(Dispatchers.IO).launch {
                val user = authManager.getCurrentUser()
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        val requiredRole = intent.getStringExtra("user_role")
                        if (user.role == requiredRole) {
                            Toast.makeText(this@LoginActivity, "Chào mừng ${user.username}", Toast.LENGTH_SHORT).show()
                            navigateToDashboard(user.role, user)
                        } else {
                            authManager.signOut()
                            Toast.makeText(this@LoginActivity, "Vai trò không hợp lệ, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        authManager.signOut()
                        Toast.makeText(this@LoginActivity, "Không thể tải dữ liệu người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etUsername.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }

    private fun navigateToDashboard(role: String?, user: User?) {
        val intent: Intent
        when (role) {
            "chef" -> {
                intent = Intent(this, ChefDashboardActivity::class.java).apply {
                    // Đảm bảo đối tượng user được truyền đi
                    putExtra("user_object", user)
                }
            }
            "manager" -> {
                intent = Intent(this, ManagerDashboardActivity::class.java).apply {
                    // Đảm bảo đối tượng user được truyền đi
                    putExtra("user_object", user)
                }
            }
            else -> {
                return
            }
        }
        startActivity(intent)
        finish()
    }
}