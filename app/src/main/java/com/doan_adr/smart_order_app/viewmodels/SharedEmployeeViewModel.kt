// File: viewmodels/SharedEmployeeViewModel.kt

package com.doan_adr.smart_order_app.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doan_adr.smart_order_app.Models.User

// Sealed class để gói gọn cả User và Password trong một sự kiện duy nhất
sealed class EmployeeEvent {
    data class AddEmployee(val user: User, val password: String) : EmployeeEvent()
    data class UpdateEmployee(val user: User) : EmployeeEvent()
}

class SharedEmployeeViewModel : ViewModel() {

    // Sử dụng SingleLiveEvent hoặc một biến LiveData để gửi sự kiện
    // Về bản chất, LiveData này sẽ chỉ chứa một sự kiện duy nhất
    private val _employeeEvent = MutableLiveData<EmployeeEvent?>()
    val employeeEvent: MutableLiveData<EmployeeEvent?> = _employeeEvent

    // Gửi sự kiện thêm hoặc cập nhật nhân viên
    fun setEmployeeEvent(user: User, password: String? = null) {
        if (password != null) {
            _employeeEvent.value = EmployeeEvent.AddEmployee(user, password)
        } else {
            _employeeEvent.value = EmployeeEvent.UpdateEmployee(user)
        }
    }

    // Xóa sự kiện sau khi đã xử lý để tránh gọi lại
    fun clearEvent() {
        _employeeEvent.value = null
    }
}