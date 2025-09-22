package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.ChefOrderAdapter
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChefOrderListFragment : Fragment() {

    private var status: String? = null
    private lateinit var recyclerView: RecyclerView
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null
    private lateinit var orderAdapter: ChefOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            status = it.getString(ARG_STATUS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chef_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv_orders)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Khởi tạo Adapter với tham số onItemClick
        orderAdapter = ChefOrderAdapter { order ->
            handleOrderAction(order)
        }
        recyclerView.adapter = orderAdapter
    }

    override fun onStart() {
        super.onStart()
        status?.let {
            listenerRegistration = firebaseManager.addRealtimeOrdersListener(it) { newOrders ->
                // Sử dụng submitList để cập nhật danh sách một cách hiệu quả
                orderAdapter.submitList(newOrders)
                Log.d("ChefOrderListFragment", "Đã nhận ${newOrders.size} đơn hàng cho trạng thái $it")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        listenerRegistration?.remove()
    }

    private fun handleOrderAction(order: Order) {
        val newStatus = when (order.status) {
            "pending" -> "cooking"
            "cooking" -> "ready"
            "ready" -> "served"
            else -> {
                Log.w("ChefOrderListFragment", "Không thể chuyển trạng thái từ: ${order.status}")
                return
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Sử dụng hàm updateOrderStatus đã được tối ưu của bạn
                firebaseManager.updateOrderStatus(order.id, newStatus)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val ARG_STATUS = "status"

        @JvmStatic
        fun newInstance(status: String) =
            ChefOrderListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status)
                }
            }
    }
}