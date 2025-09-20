package com.doan_adr.smart_order_app

import android.content.Intent
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
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.adapters.MenuAdapter
import com.doan_adr.smart_order_app.fragments.CartDialogFragment
import com.doan_adr.smart_order_app.fragments.DishDetailDialogFragment
import com.doan_adr.smart_order_app.fragments.OnlinePaymentDialogFragment
import com.doan_adr.smart_order_app.fragments.PaymentMethodDialogFragment
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import java.util.Random
import java.util.UUID

class MenuActivity : AppCompatActivity(),
    DishDetailDialogFragment.OnCartItemAddedListener,
    CartDialogFragment.OnCartItemsUpdatedListener,
    PaymentMethodDialogFragment.OnPaymentSelectedListener {

    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartBadgeCount: TextView
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private lateinit var tableId: String
    private lateinit var tableName: String
    private lateinit var menuAdapter: MenuAdapter
    private val databaseManager = FirebaseDatabaseManager()

    // Thêm các biến mới để lưu trữ thông tin đơn hàng cuối cùng
    private var finalOrderCartItems: ArrayList<CartItem> = ArrayList()
    private var finalOriginalTotalPrice: Double = 0.0
    private var finalDiscountCode: String? = null
    private var finalDiscountValue: Double = 0.0
    private var finalTotalPrice: Double = 0.0
    private var isOrderCreated = false
    private var dishesListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null
    private var allDishes: List<Dish> = emptyList()
    private lateinit var fabCart: ExtendedFloatingActionButton
    private lateinit var fabCartBadgeCount: TextView

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

        fabCart = findViewById(R.id.fab_cart)
        fabCartBadgeCount = findViewById(R.id.fab_cart_badge_count)
        fabCart.setOnClickListener {
            showCartDialog() // Gọi phương thức chung
        }

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

    /**
     * Mở CartDialogFragment để hiển thị giỏ hàng.
     */
    private fun showCartDialog() {
        val dialog = CartDialogFragment.newInstance(ArrayList(cartItems))
        dialog.show(supportFragmentManager, "CartDialog")
        Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                showCartDialog() // Gọi phương thức chung
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

    override fun onCheckout(
        cartItems: ArrayList<CartItem>,
        originalTotalPrice: Double,
        discountCode: String?,
        discountValue: Double,
        finalTotalPrice: Double
    ) {
        // Lưu trữ các giá trị cuối cùng
        this.finalOrderCartItems = cartItems
        this.finalOriginalTotalPrice = originalTotalPrice
        this.finalDiscountCode = discountCode
        this.finalDiscountValue = discountValue
        this.finalTotalPrice = finalTotalPrice

        // Mở DialogFragment thanh toán
        val paymentDialogFragment = PaymentMethodDialogFragment.newInstance()
        paymentDialogFragment.show(supportFragmentManager, "PaymentDialogFragment")
    }

    private fun updateCartIconBadge() {
        val cartCount = cartItems.sumOf { it.quantity }
        // Khai báo một danh sách chứa cả hai TextView của badge
        val badgeTextViews = listOf(cartBadgeCount, fabCartBadgeCount)

        if (cartCount > 0) {
            val countText = if (cartCount > 99) "99+" else cartCount.toString()
            badgeTextViews.forEach { badge ->
                badge.text = countText
                if (badge.visibility != View.VISIBLE) {
                    badge.apply {
                        visibility = View.VISIBLE
                        alpha = 0f
                        scaleX = 0f
                        scaleY = 0f
                        animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start()
                    }
                }
            }
        } else {
            badgeTextViews.forEach { badge ->
                if (badge.visibility != View.GONE) {
                    badge.animate()
                        .alpha(0f)
                        .scaleX(0f)
                        .scaleY(0f)
                        .setDuration(200)
                        .withEndAction {
                            badge.visibility = View.GONE
                        }
                        .start()
                }
            }
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

    fun getTableId(): String {
        return this.tableId
    }

    private fun onDishSelected(dish: Dish) {
        val dialog = DishDetailDialogFragment.newInstance(dish)
        dialog.show(supportFragmentManager, "DishDetailDialog")
    }

    /**
     * Tạo mã đơn hàng theo quy tắc đã định nghĩa.
     * [Mã Cửa Hàng]-[Ngày Tháng]-[Giờ Phút Giây]-[Số ngẫu nhiên]
     *
     * Ví dụ: SO-250918-164701-382
     *
     * SO: Mã viết tắt của cửa hàng ("Smart Order"). Bạn có thể thay đổi tùy ý.
     *
     * 250918: Ngày, tháng, năm (ngày 18 tháng 09 năm 2025).
     *
     * 164701: Giờ, phút, giây (16:47:01).
     *
     * 382: Ba chữ số ngẫu nhiên để đảm bảo không trùng lặp nếu có nhiều đơn hàng được đặt cùng một giây.
     */
    private fun generateOrderId(): String {
        val storePrefix = "QL" // Mã cửa hàng
        val now = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
        val randomSuffix = Random().nextInt(900) + 100 // Tạo số ngẫu nhiên từ 100 đến 999

        val date = dateFormat.format(now)
        val time = timeFormat.format(now)

        return "$storePrefix-$date-$time-$randomSuffix"
    }

    // Create order and navigate to OrderTrackingActivity
    override fun onPaymentSelected(method: String) {
        // Kiểm tra xem các biến đã được gán giá trị từ onCheckout chưa
        if (finalOrderCartItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào trong giỏ hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo mã đơn hàng tùy chỉnh
        val customOrderId = generateOrderId()

        val newOrder = Order(
            id = customOrderId,
            tableId = tableId,
            tableName = "Bàn số $tableId",
            cartItems = finalOrderCartItems,
            originalTotalPrice = finalOriginalTotalPrice,
            discountCode = finalDiscountCode,
            discountValue = finalDiscountValue,
            finalTotalPrice = finalTotalPrice,
            paymentMethod = method,
            paymentStatus = if (method == "cash") "pending" else "pending_online",
            status = if (method == "cash") "pending" else "pending_online"
        )
        if (method == "online"){
            // Tạo đơn hàng trên Firestore trước khi hiển thị dialog thanh toán
            lifecycleScope.launch {
                try {
                    databaseManager.createOrder(newOrder)
                    // Hiển thị dialog thanh toán trực tuyến
                    OnlinePaymentDialogFragment.newInstance(newOrder).show(supportFragmentManager, "OnlinePaymentDialogFragment")
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Lỗi khi tạo đơn hàng: ${e.message}", e)
                    Toast.makeText(this@MenuActivity, "Có lỗi xảy ra khi tạo đơn hàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            lifecycleScope.launch {
                try {
                    // Lưu đơn hàng vào Firestore
                    databaseManager.createOrder(newOrder)

                    Toast.makeText(this@MenuActivity, "Đơn hàng đã được đặt thành công!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@MenuActivity, OrderTrackingActivity::class.java).apply {
                        putExtra("orderId", newOrder.id)
                    }
                    startActivity(intent)
                    finish()
                } catch (e: Exception) {
                    Log.e("MenuActivity", "Lỗi khi tạo đơn hàng: ${e.message}", e)
                    Toast.makeText(this@MenuActivity, "Có lỗi xảy ra, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
