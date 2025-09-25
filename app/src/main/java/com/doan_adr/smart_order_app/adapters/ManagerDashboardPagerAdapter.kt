package com.doan_adr.smart_order_app.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doan_adr.smart_order_app.fragments.DiscountManagementFragment
import com.doan_adr.smart_order_app.fragments.DishManagementFragment
import com.doan_adr.smart_order_app.fragments.EmployeeManagementFragment
import com.doan_adr.smart_order_app.fragments.OrderTrackingFragment
import com.doan_adr.smart_order_app.fragments.StatisticsFragment
import com.doan_adr.smart_order_app.fragments.TableManagementFragment

class ManagerDashboardPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val tabTitles: List<String>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return tabTitles.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TableManagementFragment()
            1 -> DishManagementFragment()
            2 -> EmployeeManagementFragment()
            3 -> OrderTrackingFragment()
            // Thêm các fragment mới vào đây
            4 -> DiscountManagementFragment()
            5 -> StatisticsFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        } as Fragment
    }
}