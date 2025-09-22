package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.Models.CartItem // Import lớp CartItem

class PaymentMethodDialogFragment : DialogFragment() {

    // Định nghĩa interface để giao tiếp với Activity cha
    interface OnPaymentSelectedListener {
        fun onPaymentSelected(method: String)
    }

    private var listener: OnPaymentSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sử dụng layout có chứa các nút thanh toán
        return inflater.inflate(R.layout.dialog_payment_method, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Gán listener từ Activity cha
        if (activity is OnPaymentSelectedListener) {
            listener = activity as OnPaymentSelectedListener
        }

        // Thay đổi logic cho nút thanh toán tiền mặt
        view.findViewById<Button>(R.id.payment_cash_button).setOnClickListener {
            // Gọi phương thức trong Activity cha và truyền phương thức thanh toán là "cash"
            listener?.onPaymentSelected("cash")
            dismiss() // Đóng DialogFragment
        }

        // Thay đổi logic cho nút thanh toán trực tuyến
        view.findViewById<Button>(R.id.payment_online_button).setOnClickListener {
            // Gọi phương thức trong Activity cha và truyền phương thức thanh toán là "online"
            listener?.onPaymentSelected("online")
        }
    }

    // Đảm bảo dialog full màn hình hoặc có kích thước mong muốn
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        fun newInstance(): PaymentMethodDialogFragment {
            return PaymentMethodDialogFragment()
        }
    }
}