package com.doan_adr.smart_order_app.fragments

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.Models.Topping
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.ToppingAdapter
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration
import java.text.NumberFormat
import java.util.Locale

class DishDetailDialogFragment : DialogFragment() {

    // Views
    private lateinit var closeButton: ImageButton
    private lateinit var dishImage: ImageView
    private lateinit var dishName: TextView
    private lateinit var dishDescription: TextView
    private lateinit var dishHealthTips: TextView
    private lateinit var dishOriginalPrice: TextView
    private lateinit var dishDiscountedPrice: TextView
    private lateinit var toppingsRecyclerView: RecyclerView
    private lateinit var quantityText: TextView
    private lateinit var decrementButton: ImageButton
    private lateinit var incrementButton: ImageButton
    private lateinit var totalPrice: TextView
    private lateinit var addToCartButton: Button
    private lateinit var toppingsLabel: TextView
    private lateinit var toppingsProgressBar: ProgressBar
    private lateinit var loadingText: TextView

    // Data
    private var currentDish: Dish? = null
    private var currentQuantity: Int = 1
    private val selectedToppings: MutableList<Topping> = mutableListOf()

    // Firebase
    private val databaseManager = FirebaseDatabaseManager()
    private var toppingListener: ListenerRegistration? = null

    // Adapter
    private lateinit var toppingAdapter: ToppingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_dish_detail, container, false)

        // Bind views
        closeButton = view.findViewById(R.id.close_dialog_button)
        dishImage = view.findViewById(R.id.dialog_dish_image)
        dishName = view.findViewById(R.id.dialog_dish_name)
        dishDescription = view.findViewById(R.id.dialog_dish_description)
        dishHealthTips = view.findViewById(R.id.dialog_dish_health_tips)
        dishOriginalPrice = view.findViewById(R.id.dialog_dish_original_price)
        dishDiscountedPrice = view.findViewById(R.id.dialog_dish_discounted_price)
        toppingsRecyclerView = view.findViewById(R.id.toppings_recycler_view)
        quantityText = view.findViewById(R.id.quantity_text)
        decrementButton = view.findViewById(R.id.decrement_button)
        incrementButton = view.findViewById(R.id.increment_button)
        totalPrice = view.findViewById(R.id.total_price)
        addToCartButton = view.findViewById(R.id.add_to_cart_dialog_button)
        toppingsLabel = view.findViewById(R.id.toppings_label)
        toppingsProgressBar = view.findViewById(R.id.toppings_progress_bar)
        loadingText = view.findViewById(R.id.loading_text)

        // Adapter với callback
        toppingAdapter = ToppingAdapter(emptyList()) { topping, isChecked ->
            Log.d("ToppingCheck", "Topping: ${topping.name}, isChecked: $isChecked")
            if (isChecked) selectedToppings.add(topping) else selectedToppings.remove(topping)
            updateTotalPrice()
        }

        toppingsRecyclerView.layoutManager = LinearLayoutManager(context)
        toppingsRecyclerView.adapter = toppingAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentDish = arguments?.getParcelable("dish")

        currentDish?.let { dish ->
            // Hiển thị thông tin món ăn
            dishName.text = dish.name
            dishDescription.text = dish.description
            dishHealthTips.text = dish.healthTips
            Glide.with(this).load(dish.imageUrl).into(dishImage)

            // Hiển thị giá
            if (dish.originalPrice > dish.discountedPrice) {
                dishOriginalPrice.apply {
                    text = formatPrice(dish.originalPrice)
                    // Dòng code mới để gạch ngang giá gốc
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    visibility = View.VISIBLE
                }
                dishDiscountedPrice.text = formatPrice(dish.discountedPrice)
            } else {
                dishOriginalPrice.visibility = View.GONE
                dishDiscountedPrice.text = formatPrice(dish.originalPrice)
            }

            // Xử lý toppings
            if (dish.toppingIds.isNotEmpty()) {
                toppingsLabel.visibility = View.VISIBLE
                showToppingLoading(true)

                toppingListener = databaseManager.addToppingsListener(dish.toppingIds) { toppings ->
                    toppingAdapter.updateToppings(toppings)
                    showToppingLoading(false)
                    updateTotalPrice()
                    Log.d("DishDetailDialog", "Toppings updated: ${toppings.size}")
                }
            } else {
                toppingsLabel.visibility = View.GONE
                showToppingLoading(false)
            }
        }

        // Giá tổng ban đầu
        updateTotalPrice()

        // Events
        closeButton.setOnClickListener { dismiss() }
        decrementButton.setOnClickListener { changeQuantity(-1) }
        incrementButton.setOnClickListener { changeQuantity(1) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toppingListener?.remove()
    }

    private fun updateTotalPrice() {
        val basePrice = currentDish?.discountedPrice ?: 0.0
        val toppingsPrice = selectedToppings.sumOf { it.price }
        val total = (basePrice + toppingsPrice) * currentQuantity
        totalPrice.text = formatPrice(total)
    }

    private fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(price)
    }

    private fun changeQuantity(delta: Int) {
        val newQuantity = currentQuantity + delta
        if (newQuantity >= 1) {
            currentQuantity = newQuantity
            quantityText.text = currentQuantity.toString()
            updateTotalPrice()
        }
    }

    private fun showToppingLoading(isLoading: Boolean) {
        toppingsRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        toppingsProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loadingText.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        fun newInstance(dish: Dish): DishDetailDialogFragment {
            return DishDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("dish", dish)
                }
            }
        }
    }
}
