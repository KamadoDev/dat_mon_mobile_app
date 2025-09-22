package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.doan_adr.smart_order_app.adapters.ChefPagerAdapter
import com.doan_adr.smart_order_app.databinding.ActivityChefDashboardBinding
import com.doan_adr.smart_order_app.Models.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.doan_adr.smart_order_app.utils.AuthManager

class ChefDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChefDashboardBinding
    private val authManager = AuthManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChefDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thông tin người dùng từ Intent
        val user = intent.getParcelableExtra<User>("user_object")

        // Kiểm tra đối tượng user và các trường dữ liệu trước khi sử dụng
        if (user != null) {
            // Hiển thị tên người dùng, nếu rỗng thì dùng giá trị mặc định
            binding.tvChefName.text = if (user.username.isNotBlank()) user.username else "Đầu Bếp"

            // Tải hình đại diện bằng Glide và xử lý trường hợp URL rỗng
            Glide.with(this)
                .load(user.avatar)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.person_24px) // Ảnh placeholder
                .error(R.drawable.person_24px) // Ảnh lỗi
                .into(binding.ivChefAvatar)
        } else {
            // Trường hợp user là null (không được truyền qua Intent)
            binding.tvChefName.text = "Đầu Bếp"
            Glide.with(this)
                .load(R.drawable.person_24px) // Tải ảnh placeholder nếu không có user
                .apply(RequestOptions.circleCropTransform())
                .into(binding.ivChefAvatar)
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

        // Thêm sự kiện click cho nút đăng xuất
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