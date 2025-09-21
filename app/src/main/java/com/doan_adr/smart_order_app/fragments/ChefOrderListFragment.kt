package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.ChefOrderAdapter
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration

class ChefOrderListFragment : Fragment() {

    private var status: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChefOrderAdapter
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null

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

        // Khởi tạo adapter trống
        adapter = ChefOrderAdapter(emptyList()) { order ->
            // TODO: Xử lý sự kiện khi nhấn vào đơn hàng để xem chi tiết
        }
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        // Bắt đầu lắng nghe đơn hàng từ Firebase
        val statuses = listOf(status ?: "pending")
        listenerRegistration = firebaseManager.listenForOrdersByStatus(statuses) { orders ->
            adapter.updateOrders(orders)
        }
    }

    override fun onStop() {
        super.onStop()
        // Dừng lắng nghe khi Fragment không còn hiển thị
        listenerRegistration?.remove()
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