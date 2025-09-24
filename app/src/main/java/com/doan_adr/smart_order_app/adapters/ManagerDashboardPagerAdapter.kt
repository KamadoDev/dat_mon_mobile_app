package com.doan_adr.smart_order_app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doan_adr.smart_order_app.fragments.DishManagementFragment
import com.doan_adr.smart_order_app.fragments.EmployeeManagementFragment
import com.doan_adr.smart_order_app.fragments.OrderTrackingFragment

class ManagerDashboardPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tabTitles: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return tabTitles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DishManagementFragment()      // Quản lý món ăn
            1 -> EmployeeManagementFragment()  // Quản lý nhân viên
            2 -> OrderTrackingFragment()       // Theo dõi đơn hàng
            else -> throw IllegalArgumentException("Invalid tab position")
        } as Fragment
    }
}