package com.doan_adr.smart_order_app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.doan_adr.smart_order_app.OrderTrackingActivity
import com.doan_adr.smart_order_app.R
import com.doan_adr.smart_order_app.Models.Order
import com.doan_adr.smart_order_app.utils.FirebaseDatabaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OnlinePaymentDialogFragment : DialogFragment() {

    private lateinit var finalTotalPriceText: TextView
    private lateinit var paymentStatusText: TextView
    private lateinit var progressBar: ProgressBar

    private var order: Order? = null
    private val databaseManager = FirebaseDatabaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        order = arguments?.getParcelable(ARG_ORDER)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Sử dụng layout mới
        return inflater.inflate(R.layout.dialog_online_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        finalTotalPriceText = view.findViewById(R.id.final_total_price_text)
        paymentStatusText = view.findViewById(R.id.payment_status_text)
        progressBar = view.findViewById(R.id.payment_progress_bar)

        // Hiển thị thông tin đơn hàng
        order?.let {
            val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            finalTotalPriceText.text = "Tổng tiền: ${format.format(it.finalTotalPrice)}"
            // Trạng thái ban đầu
            paymentStatusText.text = "Trạng thái: Đang chờ thanh toán"

            // Bắt đầu mô phỏng quá trình thanh toán
            simulatePayment(it.id)
        } ?: run {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy thông tin đơn hàng.", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    private fun simulatePayment(orderId: String) {
        lifecycleScope.launch {
            try {
                // Giả lập thời gian thanh toán
                delay(3000) // Đợi 3 giây

                // Cập nhật trạng thái thanh toán trên Firestore
                databaseManager.updatePaymentStatus(orderId, "paid")

                // Cập nhật UI
                paymentStatusText.text = "Trạng thái: Đã thanh toán thành công!"
                paymentStatusText.setTextColor(resources.getColor(R.color.green))
                progressBar.visibility = View.GONE

                Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show()

                // Đợi thêm 1.5 giây trước khi chuyển màn hình
                delay(1500)
                dismiss() // Đóng dialog

                // Chuyển đến màn hình theo dõi đơn hàng
                val intent = Intent(requireContext(), OrderTrackingActivity::class.java).apply {
                    putExtra("orderId", orderId)
                }
                startActivity(intent)
                requireActivity().finish()

            } catch (e: Exception) {
                // Xử lý lỗi nếu có
                paymentStatusText.text = "Trạng thái: Lỗi khi thanh toán!"
                paymentStatusText.setTextColor(resources.getColor(R.color.red))
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Lỗi khi thanh toán: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    companion object {
        private const val ARG_ORDER = "order"
        fun newInstance(order: Order): OnlinePaymentDialogFragment {
            return OnlinePaymentDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ORDER, order)
                }
            }
        }
    }
}