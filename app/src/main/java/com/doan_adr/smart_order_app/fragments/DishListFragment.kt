package com.doan_adr.smart_order_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doan_adr.smart_order_app.Models.Category
import com.doan_adr.smart_order_app.Models.Dish
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.adapters.MenuAdapter

class DishListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dishList: List<Dish>
    private lateinit var onDishClick: (Dish) -> Unit

    companion object {
        private const val ARG_DISH_LIST = "dish_list"

        fun newInstance(dishList: List<Dish>, onDishClick: (Dish) -> Unit): DishListFragment {
            val fragment = DishListFragment()
            fragment.dishList = dishList
            fragment.onDishClick = onDishClick
            return fragment
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dish_list, container, false)
        recyclerView = view.findViewById(R.id.dishes_recycler_view)

        // Thiết lập RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        val adapter = MenuAdapter(requireContext(), dishList) { dish ->
            onDishClick(dish)
        }
        recyclerView.adapter = adapter
        return view
    }
}