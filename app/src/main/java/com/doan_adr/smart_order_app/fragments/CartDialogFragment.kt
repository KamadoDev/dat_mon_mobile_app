// File: CartDialogFragment.kt
package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.CartItem
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.CartAdapter
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.doan_adr.smart_order_app.Models.Discount
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Locale

class CartDialogFragment : DialogFragment(), CartAdapter.OnItemActionListener {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var emptyCartText: TextView
    private lateinit var totalPriceText: TextView
    private lateinit var checkoutButton: Button

    private lateinit var discountCodeEditText: EditText
    private lateinit var applyDiscountButton: Button
    private lateinit var discountInfoTextView: TextView
    private lateinit var subtotalPriceText: TextView

    private var cartItems: ArrayList<CartItem> = ArrayList()
    private var listener: OnCartItemsUpdatedListener? = null
    private var appliedDiscount: Discount? = null

    interface OnCartItemsUpdatedListener {
        fun onCartItemsUpdated(updatedItems: ArrayList<CartItem>)
        fun onCheckout(
            cartItems: ArrayList<CartItem>,
            originalTotalPrice: Double,
            discountCode: String?,
            discountValue: Double,
            finalTotalPrice: Double
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        cartItems = arguments?.getParcelableArrayList(ARG_CART_ITEMS) ?: ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_cart, container, false)
        cartRecyclerView = view.findViewById(R.id.cart_recycler_view)
        emptyCartText = view.findViewById(R.id.empty_cart_text)
        totalPriceText = view.findViewById(R.id.total_price_text)
        checkoutButton = view.findViewById(R.id.checkout_button)

        discountCodeEditText = view.findViewById(R.id.discount_code_edit_text)
        applyDiscountButton = view.findViewById(R.id.apply_discount_button)
        discountInfoTextView = view.findViewById(R.id.discount_info_text_view)
        subtotalPriceText = view.findViewById(R.id.subtotal_price_text)

        // **Đoạn mã đã được di chuyển và tối ưu**
        view.post {
            val window = dialog?.window
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val controller = WindowInsetsControllerCompat(window, view)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        val toolbar: Toolbar = view.findViewById(R.id.cart_toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        updateCartState()
        checkoutButton.setOnClickListener {
            val originalTotal = cartItems.sumOf { it.totalPrice }
            var discountValue = 0.0
            var finalTotal = originalTotal

            appliedDiscount?.let { discount ->
                discountValue = when (discount.discountType) {
                    "percentage" -> (originalTotal * (discount.value / 100)).coerceAtMost(discount.maxDiscount)
                    "fixed" -> discount.value
                    else -> 0.0
                }
                finalTotal = originalTotal - discountValue
            }

            listener?.onCheckout(
                cartItems,
                originalTotal,
                appliedDiscount?.code,
                discountValue,
                finalTotal
            )
        }

        applyDiscountButton.setOnClickListener {
            val discountCode = discountCodeEditText.text.toString()
            if (discountCode.isNotEmpty()) {
                checkAndApplyDiscount(discountCode)
            } else {
                Toast.makeText(context, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show()
            }
        }

        cartRecyclerView.layoutManager = LinearLayoutManager(context)
        cartRecyclerView.adapter = CartAdapter(cartItems, this)

        return view
    }

    override fun onQuantityChanged(cartItem: CartItem, newQuantity: Int) {
        val item = cartItems.find { it.id == cartItem.id }
        if (item != null) {
            item.quantity = newQuantity
            item.totalPrice = item.unitPrice * newQuantity
            (cartRecyclerView.adapter as? CartAdapter)?.updateItems(cartItems)
            listener?.onCartItemsUpdated(cartItems)
            updateCartState()
        }
    }

    override fun onItemRemoved(cartItem: CartItem) {
        cartItems.removeIf { it.id == cartItem.id }
        (cartRecyclerView.adapter as? CartAdapter)?.updateItems(cartItems)
        listener?.onCartItemsUpdated(cartItems)
        updateCartState()
    }

    private fun checkAndApplyDiscount(code: String) {
        val databaseManager = FirebaseDatabaseManager()
        viewLifecycleOwner.lifecycleScope.launch {
            val discount = databaseManager.getDiscountByCode(code)

            if (discount != null) {
                if (discount.usageLimit > 0) {
                    if (cartItems.sumOf { it.totalPrice } >= discount.minOrderValue) {
                        appliedDiscount = discount
                        updateTotalPrice()
                        Toast.makeText(context, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show()
                        discount.id?.let {
                            databaseManager.updateDiscountUsage(
                                discountId = it,
                                currentUsage = discount.usageLimit,
                                currentTimesUsed = discount.timesUsed
                            )
                        }
                    } else {
                        Toast.makeText(context, "Đơn hàng chưa đủ điều kiện áp dụng mã này.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Mã giảm giá đã hết lượt sử dụng.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Mã giảm giá không hợp lệ.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCartState() {
        if (cartItems.isEmpty()) {
            emptyCartText.visibility = View.VISIBLE
            cartRecyclerView.visibility = View.GONE
            totalPriceText.visibility = View.GONE
            checkoutButton.visibility = View.GONE
        } else {
            emptyCartText.visibility = View.GONE
            cartRecyclerView.visibility = View.VISIBLE
            totalPriceText.visibility = View.VISIBLE
            checkoutButton.visibility = View.VISIBLE
            updateTotalPrice()
        }
    }

    private fun updateTotalPrice() {
        val subtotal = cartItems.sumOf { it.totalPrice }
        var finalTotal = subtotal
        var discountAmount = 0.0

        appliedDiscount?.let { discount ->
            discountAmount = when (discount.discountType) {
                "percentage" -> (subtotal * (discount.value / 100)).coerceAtMost(discount.maxDiscount)
                "fixed" -> discount.value
                else -> 0.0
            }
            finalTotal = subtotal - discountAmount
        }

        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        subtotalPriceText.text = format.format(subtotal)

        if (appliedDiscount != null) {
            subtotalPriceText.visibility = View.VISIBLE
            discountInfoTextView.text = "- ${format.format(discountAmount)}"
            discountInfoTextView.visibility = View.VISIBLE
        } else {
            subtotalPriceText.visibility = View.GONE
            discountInfoTextView.visibility = View.GONE
        }

        totalPriceText.text = format.format(finalTotal)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onResume() {
        super.onResume()
        listener = activity as? OnCartItemsUpdatedListener
    }

    companion object {
        private const val ARG_CART_ITEMS = "cart_items"

        fun newInstance(cartItems: ArrayList<CartItem>): CartDialogFragment {
            return CartDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CART_ITEMS, cartItems)
                }
            }
        }
    }
}