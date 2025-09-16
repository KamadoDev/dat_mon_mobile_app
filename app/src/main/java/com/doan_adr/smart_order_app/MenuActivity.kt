package com.doan_adr.smart_order_app

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.Models.Topping
import com.doan_adr.smart_order_app.adapters.MenuAdapter
import com.doan_adr.smart_order_app.adapters.ToppingAdapter
import com.doan_adr.smart_order_app.fragments.DishDetailDialogFragment
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MenuActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tableId: String
    private lateinit var tableName: String
    private lateinit var menuAdapter: MenuAdapter
    private val databaseManager = FirebaseDatabaseManager()
    private var isOrderCreated = false // Biến cờ để kiểm tra đã tạo đơn hàng chưa

    // Biến để quản lý các real-time listeners
    private var dishesListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null

    // Cache dữ liệu trong bộ nhớ để lọc nhanh
    private var allDishes: List<Dish> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Nhận dữ liệu từ Intent
        tableId = intent.getStringExtra("tableId") ?: return
        tableName = intent.getStringExtra("tableName") ?: return

        // Ánh xạ các views từ layout
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.menu_recycler_view)

        // Thiết lập Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Thực đơn bàn $tableName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Xử lý sự kiện khi nhấn nút back trên Toolbar
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Khởi tạo Adapter và RecyclerView
        menuAdapter = MenuAdapter(this, emptyList()) { dish ->
            onDishSelected(dish)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = menuAdapter

        // Thiết lập lắng nghe sự kiện khi chọn tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val categoryId = tab?.tag as? String ?: "all"
                Log.d("MenuActivity", "Đã chọn danh mục: $categoryId")
                // Lọc món ăn từ cache cục bộ
                updateDishesForCategory(categoryId)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Xử lý sự kiện khi nhấn nút back của thiết bị
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // Đăng ký lại listeners khi Activity được hiển thị
        setupListeners()
    }

    override fun onPause() {
        super.onPause()
        // Hủy đăng ký listeners khi Activity không còn hiển thị để tránh rò rỉ bộ nhớ
        dishesListener?.remove()
        categoriesListener?.remove()
    }

    // Thêm phương thức này để xử lý khi ứng dụng bị dừng đột ngột
    override fun onStop() {
        super.onStop()
        // Nếu Activity sắp bị hủy (do người dùng chủ động thoát) thì bỏ qua
        // Nếu không (do hệ thống dừng hoặc người dùng vuốt ứng dụng), thì mở khóa bàn
        if (!isOrderCreated && !isFinishing) {
            lifecycleScope.launch {
                try {
                    databaseManager.unlockTable(tableId)
                    Log.d("MenuActivity", "Ứng dụng bị dừng đột ngột, đã mở khóa bàn: $tableId")
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Lỗi khi mở khóa bàn trong onStop: ${e.message}")
                }
            }
        }
    }

    // Ghi đè phương thức để tạo menu trên Toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        return true
    }

    // Ghi đè phương thức để xử lý sự kiện click vào các item trên menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                // Xử lý khi người dùng nhấn vào biểu tượng giỏ hàng
                Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupListeners() {
        // Lắng nghe thay đổi của Categories
        categoriesListener = databaseManager.addCategoriesListener { categories ->
            updateCategoryTabs(categories)
        }

        // Lắng nghe thay đổi của Dishes
        dishesListener = databaseManager.addDishesListener { dishes ->
            // Cập nhật cache cục bộ mỗi khi có thay đổi từ Firebase
            allDishes = dishes
            // Cập nhật danh sách món ăn cho tab đang được chọn
            val selectedTabTag = tabLayout.selectedTabPosition.let {
                if (it != -1) tabLayout.getTabAt(it)?.tag as? String else "all"
            } ?: "all"
            updateDishesForCategory(selectedTabTag)
        }
    }

    private fun updateCategoryTabs(categories: List<Category>) {
        tabLayout.removeAllTabs()

        // Thêm thủ công tab "Tất cả"
        val allTab = tabLayout.newTab()
        allTab.text = "Tất cả"
        allTab.tag = "all"
        tabLayout.addTab(allTab)

        // Thêm các danh mục còn lại từ Firebase
        categories.forEach { category ->
            val tab = tabLayout.newTab()
            tab.text = category.name
            tab.tag = category.id
            tabLayout.addTab(tab)
        }
    }

    private fun updateDishesForCategory(categoryId: String) {
        val filteredDishes = if (categoryId == "all") {
            allDishes
        } else {
            allDishes.filter { it.categoryId == categoryId }
        }
        menuAdapter.updateDishes(filteredDishes)
    }

    private fun handleBackPress() {
        if (!isOrderCreated) {
            // Nếu chưa có đơn hàng, mở khóa bàn
            lifecycleScope.launch {
                try {
                    databaseManager.unlockTable(tableId)
                    Toast.makeText(this@MenuActivity, "Đã khôi phục trạng thái bàn.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Lỗi khi mở khóa bàn: ${e.message}")
                } finally {
                    // Sau khi xử lý, thoát Activity
                    finish()
                }
            }
        } else {
            // Nếu đã có đơn hàng, chỉ cần thoát Activity bình thường
            finish()
        }
    }

    private fun onDishSelected(dish: Dish) {
        // Khởi tạo DishDetailDialogFragment và truyền đối tượng Dish
        val dialog = DishDetailDialogFragment.newInstance(dish)

        // Hiển thị dialog
        dialog.show(supportFragmentManager, "DishDetailDialog")
    }
}