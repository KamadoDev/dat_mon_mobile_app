package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.adapters.ManagerOrderFullTrackingAdapter
import com.doan_adr.smart_order_app.databinding.FragmentOrderTrackingBinding
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import com.google.firebase.firestore.ListenerRegistration

class OrderTrackingFragment : Fragment() {

    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: ManagerOrderFullTrackingAdapter
    private val firebaseManager = FirebaseDatabaseManager()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        listenToAllOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = ManagerOrderFullTrackingAdapter(mutableListOf())

        binding.orderRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    /**
     * Lắng nghe tất cả các đơn hàng theo thời gian thực để cập nhật UI.
     */
    private fun listenToAllOrders() {
        listenerRegistration = firebaseManager.getRealtimeAllOrders { orders ->
            orderAdapter.updateData(orders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
        _binding = null
    }
}