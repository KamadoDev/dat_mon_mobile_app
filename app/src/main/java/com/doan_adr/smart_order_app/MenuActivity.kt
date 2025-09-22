package com.doan_adr.smart_order_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.fragments.CartDialogFragment
import com.doan_adr.smart_order_app.fragments.DishDetailDialogFragment
import com.doan_adr.smart_order_app.fragments.DishListFragment
import com.doan_adr.smart_order_app.fragments.OnlinePaymentDialogFragment
import com.doan_adr.smart_order_app.fragments.PaymentMethodDialogFragment
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MenuActivity : AppCompatActivity(),
    DishDetailDialogFragment.OnCartItemAddedListener,
    CartDialogFragment.OnCartItemsUpdatedListener,
    PaymentMethodDialogFragment.OnPaymentSelectedListener {

    // Properties for UI components
    private lateinit var toolbar: Toolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabCart: ExtendedFloatingActionButton
    private lateinit var fabCartBadgeCount: TextView
    private lateinit var tableId: String
    private lateinit var tableName: String

    // Data-related properties
    private val cartItems: MutableList<CartItem> = mutableListOf()
    private val databaseManager = FirebaseDatabaseManager()
    private var isOrderCreated = false
    private var allDishes: List<Dish> = emptyList()
    private var categories: List<Category> = emptyList()

    // Firestore listeners
    private var dishesListener: ListenerRegistration? = null
    private var categoriesListener: ListenerRegistration? = null

    // Final order details
    private var finalOrderCartItems: ArrayList<CartItem> = ArrayList()
    private var finalOriginalTotalPrice: Double = 0.0
    private var finalDiscountCode: String? = null
    private var finalDiscountValue: Double = 0.0
    private var finalTotalPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Get data from intent
        tableId = intent.getStringExtra("tableId") ?: return
        tableName = intent.getStringExtra("tableName") ?: return

        setupViews()
        setupToolbar()
        setupListeners()
        fetchData()
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        fabCart = findViewById(R.id.fab_cart)
        fabCartBadgeCount = findViewById(R.id.fab_cart_badge_count)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Thực đơn $tableName"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        fabCart.setOnClickListener { showCartDialog() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        })

        categoriesListener = databaseManager.addCategoriesListener { fetchedCategories ->
            categories = fetchedCategories
            setupViewPagerWithTabs()
        }

        dishesListener = databaseManager.addDishesListener { fetchedDishes ->
            allDishes = fetchedDishes
            // This is handled by ViewPager's adapter, no manual update needed
        }
    }

    private fun fetchData() {
        // Data listeners are set up in setupListeners(),
        // they will trigger and update the UI automatically.
        // No manual data fetching is needed here.
    }

    private fun setupViewPagerWithTabs() {
        val pagerAdapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = categories.size

            override fun createFragment(position: Int): androidx.fragment.app.Fragment {
                val category = categories[position]
                val dishesForCategory = allDishes.filter { it.categoryId == category.id }
                return DishListFragment.newInstance(dishesForCategory) { dish ->
                    showDishDetailDialog(dish)
                }
            }
        }
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = categories[position].name
        }.attach()
    }

    private fun showDishDetailDialog(dish: Dish) {
        DishDetailDialogFragment.newInstance(dish).show(supportFragmentManager, "DishDetailDialog")
    }

    private fun showCartDialog() {
        CartDialogFragment.newInstance(ArrayList(cartItems))
            .show(supportFragmentManager, "CartDialog")
        Toast.makeText(this, "Mở giỏ hàng", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Re-attach listeners in case they were removed in onPause
        setupListeners()
    }

    override fun onPause() {
        super.onPause()
        // Remove listeners to prevent memory leaks
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
        this.finalOrderCartItems = cartItems
        this.finalOriginalTotalPrice = originalTotalPrice
        this.finalDiscountCode = discountCode
        this.finalDiscountValue = discountValue
        this.finalTotalPrice = finalTotalPrice

        PaymentMethodDialogFragment.newInstance().show(supportFragmentManager, "PaymentDialogFragment")
    }

    private fun updateCartIconBadge() {
        val cartCount = cartItems.sumOf { it.quantity }
        if (cartCount > 0) {
            val countText = if (cartCount > 99) "99+" else cartCount.toString()
            fabCartBadgeCount.text = countText
            if (fabCartBadgeCount.visibility != View.VISIBLE) {
                fabCartBadgeCount.apply {
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
        } else {
            if (fabCartBadgeCount.visibility != View.GONE) {
                fabCartBadgeCount.animate()
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction {
                        fabCartBadgeCount.visibility = View.GONE
                    }
                    .start()
            }
        }
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

    private fun generateOrderId(): String {
        val storePrefix = "QL"
        val now = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HHmmss", Locale.getDefault())
        val randomSuffix = Random().nextInt(900) + 100

        val date = dateFormat.format(now)
        val time = timeFormat.format(now)

        return "$storePrefix-$date-$time-$randomSuffix"
    }

    override fun onPaymentSelected(method: String) {
        if (finalOrderCartItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào trong giỏ hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val newOrder = Order(
            id = generateOrderId(),
            tableId = tableId,
            tableName = tableName,
            cartItems = finalOrderCartItems,
            originalTotalPrice = finalOriginalTotalPrice,
            discountCode = finalDiscountCode,
            discountValue = finalDiscountValue,
            finalTotalPrice = finalTotalPrice,
            paymentMethod = method,
            paymentStatus = if (method == "cash") "pending" else "pending_online",
            status = "pending"
        )

        lifecycleScope.launch {
            try {
                databaseManager.createOrder(newOrder)
                isOrderCreated = true // Đánh dấu đã tạo đơn hàng
                if (method == "online") {
                    OnlinePaymentDialogFragment.newInstance(newOrder).show(supportFragmentManager, "OnlinePaymentDialogFragment")
                } else {
                    Toast.makeText(this@MenuActivity, "Đơn hàng đã được đặt thành công!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@MenuActivity, OrderTrackingActivity::class.java).apply {
                        putExtra("orderId", newOrder.id)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Log.e("MenuActivity", "Lỗi khi tạo đơn hàng: ${e.message}", e)
                Toast.makeText(this@MenuActivity, "Có lỗi xảy ra khi tạo đơn hàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}