package com.doan_adr.smart_order_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.doan_adr.smart_order_app.adapters.ChefPagerAdapter
import com.doan_adr.smart_order_app.databinding.ActivityChefDashboardBinding
import com.doan_adr.smart_order_app.Models.User
import com.bumptech.glide.Glide // Import Glide
import com.bumptech.glide.request.RequestOptions // Optional: for transformations

class ChefDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChefDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChefDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thông tin người dùng từ Intent
        val user = intent.getParcelableExtra<User>("user_object")

        if (user != null) {
            // Hiển thị tên người dùng
            binding.tvChefName.text = user.username

            // Tải hình đại diện bằng Glide và hiển thị trên ImageView
            Glide.with(this)
                .load(user.avatar) // Load từ URL
                .apply(RequestOptions.circleCropTransform()) // Tùy chọn: biến hình ảnh thành hình tròn
                .placeholder(R.drawable.ic_launcher_background) // Hình ảnh placeholder trong khi tải
                .error(R.drawable.ic_launcher_background) // Hình ảnh khi có lỗi
                .into(binding.ivChefAvatar) // Hiển thị trên ImageView
        }

        // Thiết lập ViewPager2 với Adapter
        val tabTitles = listOf("Đơn mới", "Đang nấu", "Đã xong")
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ChefPagerAdapter(this, tabTitles)
        viewPager.adapter = adapter

        // Kết nối TabLayout với ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
}