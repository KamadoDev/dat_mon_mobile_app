package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.doan_adr.smart_order_app.adapters.ManagerDashboardPagerAdapter
import com.doan_adr.smart_order_app.databinding.ActivityManagerDashboardBinding
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.utils.AuthManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@Suppress("DEPRECATION")
class ManagerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManagerDashboardBinding
    private val authManager = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.getParcelableExtra<User>("user_object")

        if (user != null) {
            binding.tvManagerName.text = if (user.username.isNotBlank()) user.username else "Quản Lý"
            Glide.with(this)
                .load(user.avatar)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.person_24px)
                .error(R.drawable.person_24px)
                .into(binding.ivManagerAvatar)
        } else {
            binding.tvManagerName.text = "Quản Lý"
            Glide.with(this)
                .load(R.drawable.person_24px)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivManagerAvatar)
        }

        // Cập nhật danh sách tabTitles
        val tabTitles = listOf("Quản lý bàn", "Quản lý món ăn", "Quản lý nhân viên", "Theo dõi đơn hàng", "Quản lý khuyến mãi", "Thống kê")
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ManagerDashboardPagerAdapter(this, tabTitles)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.btnLogout.setOnClickListener {
            authManager.signOut()
            navigateToRoleSelection()
        }
    }

    private fun navigateToRoleSelection() {
        val intent = Intent(this, RoleSelectionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}