package com.doan_adr.smart_order_app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doan_adr.smart_order_app.fragments.ChefOrderListFragment

class ChefPagerAdapter(fragmentActivity: FragmentActivity, private val tabTitles: List<String>) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        val status: String = when (position) {
            0 -> "pending" // Đơn mới
            1 -> "cooking" // Đang nấu
            2 -> "ready"   // Đã xong
            else -> "pending"
        }
        return ChefOrderListFragment.newInstance(status)
    }
}