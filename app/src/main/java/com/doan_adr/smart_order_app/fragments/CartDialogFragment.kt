package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import java.util.ArrayList
import java.io.Serializable

class CartDialogFragment : DialogFragment(), CartAdapter.OnItemActionListener {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var emptyCartText: TextView
    private var cartItems: ArrayList<CartItem> = ArrayList()
    private var listener: OnCartItemsUpdatedListener? = null

    interface OnCartItemsUpdatedListener {
        fun onCartItemsUpdated(updatedItems: ArrayList<CartItem>)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        cartItems = arguments?.getSerializable(ARG_CART_ITEMS) as? ArrayList<CartItem> ?: ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_cart, container, false)
        cartRecyclerView = view.findViewById(R.id.cart_recycler_view)
        emptyCartText = view.findViewById(R.id.empty_cart_text)

        val window = dialog?.window ?: return view
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val toolbar: Toolbar = view.findViewById(R.id.cart_toolbar)
        toolbar.setNavigationOnClickListener { dismiss() }

        if (cartItems.isEmpty()) {
            emptyCartText.visibility = View.VISIBLE
            cartRecyclerView.visibility = View.GONE
        } else {
            emptyCartText.visibility = View.GONE
            cartRecyclerView.visibility = View.VISIBLE
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
        }
    }

    override fun onItemRemoved(cartItem: CartItem) {
        cartItems.removeIf { it.id == cartItem.id }
        (cartRecyclerView.adapter as? CartAdapter)?.updateItems(cartItems)
        listener?.onCartItemsUpdated(cartItems)
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
                    putSerializable(ARG_CART_ITEMS, cartItems as Serializable)
                }
            }
        }
    }
}