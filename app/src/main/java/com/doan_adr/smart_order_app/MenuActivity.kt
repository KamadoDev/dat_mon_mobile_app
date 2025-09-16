package com.doan_adr.smart_order_app

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.adapters.MenuAdapter
import com.doan_adr.smart_order_app.fragments.CartDialogFragment
import com.doan_adr.smart_order_app.fragments.DishDetailDialogFragment
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.util.ArrayList

@OptIn(ExperimentalBadgeUtils::class)
class MenuActivity : AppCompatActivity(),
    DishDetailDialogFragment.OnCartItemAddedListener,
    CartDialogFragment.OnCartItemsUpdatedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private var cartMenuItem: MenuItem? = null
    private lateinit var cartBadgeCount: TextView
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private lateinit var tableId: String
    private lateinit var tableName: String
    private lateinit var menuAdapter: MenuAdapter
    private val databaseManager = FirebaseDatabaseManager()
    private var isOrderCreated = false

    private var dishesListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null

    private var allDishes: List<Dish> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        tableId = intent.getStringExtra("tableId") ?: return
        tableName = intent.getStringExtra("tableName") ?: return

        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        recyclerView = findViewById(R.id.menu_recycler_view)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Thực đơn bàn $tableName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        menuAdapter = MenuAdapter(this, emptyList()) { dish ->
            onDishSelected(dish)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = menuAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val categoryId = tab?.tag as? String ?: "all"
                Log.d("MenuActivity", "Đã chọn danh mục: $categoryId")
                updateDishesForCategory(categoryId)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupListeners()
    }

    override fun onPause() {
        super.onPause()
        dishesListener?.remove()
        categoriesListener?.remove()
    }

    override fun onStop() {
        super.onStop()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cart, menu)
        val cartItem = menu?.findItem(R.id.action_cart)
        val actionView = cartItem?.actionView

        // Gán TextView từ layout tùy chỉnh vào biến
        cartBadgeCount = actionView?.findViewById(R.id.cart_badge_count)!!

        // Thiết lập sự kiện click cho toàn bộ actionView
        actionView.setOnClickListener {
            onOptionsItemSelected(cartItem)
        }

        updateCartIconBadge()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                val dialog = CartDialogFragment.newInstance(ArrayList(cartItems))
                dialog.show(supportFragmentManager, "CartDialog")
                Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCartItemAdded(cartItem: CartItem) {
        val existingItem = cartItems.find {
            it.dishId == cartItem.dishId && it.toppings == cartItem.toppings && it.note == cartItem.note
        }

        if (existingItem != null) {
            existingItem.quantity += cartItem.quantity
            existingItem.totalPrice += cartItem.totalPrice
            Toast.makeText(this, "Đã cập nhật số lượng món ${existingItem.dishName}", Toast.LENGTH_SHORT).show()
        } else {
            cartItems.add(cartItem)
            Toast.makeText(this, "Đã thêm món ${cartItem.dishName} vào giỏ", Toast.LENGTH_SHORT).show()
        }

        updateCartIconBadge()
        Log.d("MenuActivity", "Giỏ hàng hiện có: ${cartItems.size} món.")
    }

    override fun onCartItemsUpdated(updatedItems: ArrayList<CartItem>) {
        cartItems.clear()
        cartItems.addAll(updatedItems)
        updateCartIconBadge()
    }

    private fun updateCartIconBadge() {
        val cartCount = cartItems.sumOf { it.quantity }
        if (cartCount > 0) {
            cartBadgeCount.text = if (cartCount > 99) "99+" else cartCount.toString()
            cartBadgeCount.visibility = View.VISIBLE
        } else {
            cartBadgeCount.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        categoriesListener = databaseManager.addCategoriesListener { categories ->
            updateCategoryTabs(categories)
        }

        dishesListener = databaseManager.addDishesListener { dishes ->
            allDishes = dishes
            val selectedTabTag = tabLayout.selectedTabPosition.let {
                if (it != -1) tabLayout.getTabAt(it)?.tag as? String else "all"
            } ?: "all"
            updateDishesForCategory(selectedTabTag)
        }
    }

    private fun updateCategoryTabs(categories: List<Category>) {
        tabLayout.removeAllTabs()
        val allTab = tabLayout.newTab()
        allTab.text = "Tất cả"
        allTab.tag = "all"
        tabLayout.addTab(allTab)

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
            lifecycleScope.launch {
                try {
                    databaseManager.unlockTable(tableId)
                    Toast.makeText(this@MenuActivity, "Đã khôi phục trạng thái bàn.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Lỗi khi mở khóa bàn: ${e.message}")
                } finally {
                    finish()
                }
            }
        } else {
            finish()
        }
    }

    private fun onDishSelected(dish: Dish) {
        val dialog = DishDetailDialogFragment.newInstance(dish)
        dialog.show(supportFragmentManager, "DishDetailDialog")
    }
}