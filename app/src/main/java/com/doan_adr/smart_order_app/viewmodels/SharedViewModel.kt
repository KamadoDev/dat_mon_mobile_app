package com.doan_adr.smart_order_app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doan_adr.smart_order_app.Models.Dish

@Suppress("UNCHECKED_CAST")
class SharedViewModel : ViewModel() {

    private val _newDish = MutableLiveData<Dish?>()
    val newDish: LiveData<Dish> = _newDish as LiveData<Dish>

    fun setNewDish(dish: Dish) {
        _newDish.value = dish
    }

    fun clearNewDish() {
        _newDish.value = null
    }
}