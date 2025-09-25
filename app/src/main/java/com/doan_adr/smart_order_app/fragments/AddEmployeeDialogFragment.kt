package com.doan_adr.smart_order_app.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.doan_adr.smart_order_app.Models.User
import com.doan_adr.smart_order_app.databinding.DialogAddEmployeeBinding
import com.doan_adr.smart_order_app.viewmodels.SharedEmployeeViewModel
import com.bumptech.glide.Glide

class AddEmployeeDialogFragment : DialogFragment() {

    private var _binding: DialogAddEmployeeBinding? = null
    private val binding get() = _binding!!
    private var userToEdit: User? = null

    private val sharedViewModel: SharedEmployeeViewModel by activityViewModels()

    companion object {
        fun newInstance(user: User): AddEmployeeDialogFragment {
            val args = Bundle().apply {
                putParcelable("user_to_edit", user)
            }
            val fragment = AddEmployeeDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userToEdit = arguments?.getParcelable("user_to_edit")
        setupViews()
        setupListeners()
        setupAvatarUrlWatcher()
    }

    private fun setupViews() {
        if (userToEdit != null) {
            binding.tvEmployeeDialogTitle.text = "Chỉnh Sửa Nhân Viên"
            binding.btnEmployeeAddOrSave.text = "Lưu"
            binding.etEmployeeUsername.setText(userToEdit?.username)
            binding.etEmployeeEmail.setText(userToEdit?.email)
            binding.etEmployeeEmail.isEnabled = false
            binding.tilEmployeePassword.visibility = View.GONE
            binding.etEmployeeAvatarUrl.setText(userToEdit?.avatar)
            binding.actvEmployeeRole.setText(userToEdit?.role, false)
        } else {
            binding.tvEmployeeDialogTitle.text = "Thêm Nhân Viên Mới"
            binding.btnEmployeeAddOrSave.text = "Thêm"
            binding.etEmployeeEmail.isEnabled = true
            binding.tilEmployeePassword.visibility = View.VISIBLE
        }
        val roles = listOf("chef", "manager")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvEmployeeRole.setAdapter(adapter)

        userToEdit?.avatar?.let { url ->
            if (url.isNotEmpty()) {
                Glide.with(this)
                    .load(url)
                    .placeholder(com.google.android.material.R.drawable.mtrl_ic_error)
                    .error(com.google.android.material.R.drawable.mtrl_ic_error)
                    .into(binding.ivEmployeeAvatarPreview)
            } else {
                binding.ivEmployeeAvatarPreview.setImageResource(com.doan_adr.smart_order_app.R.drawable.image_24px)
            }
        } ?: run {
            binding.ivEmployeeAvatarPreview.setImageResource(com.doan_adr.smart_order_app.R.drawable.image_24px)
        }
    }

    private fun setupListeners() {
        binding.btnEmployeeCancel.setOnClickListener {
            dismiss()
        }

        binding.btnEmployeeAddOrSave.setOnClickListener {
            Log.d("AddEmployeeDialogFragment", "Nút 'Thêm' đã được nhấn. Bắt đầu validate dữ liệu.")
            validateAndSaveUser()
        }
    }

    private fun setupAvatarUrlWatcher() {
        binding.etEmployeeAvatarUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val url = s.toString().trim()
                if (url.isNotEmpty()) {
                    Glide.with(this@AddEmployeeDialogFragment)
                        .load(url)
                        .placeholder(com.google.android.material.R.drawable.mtrl_ic_error)
                        .error(com.google.android.material.R.drawable.mtrl_ic_error)
                        .into(binding.ivEmployeeAvatarPreview)
                } else {
                    binding.ivEmployeeAvatarPreview.setImageResource(com.doan_adr.smart_order_app.R.drawable.image_24px)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAndSaveUser() {
        val username = binding.etEmployeeUsername.text?.toString()?.trim() ?: ""
        val email = binding.etEmployeeEmail.text?.toString()?.trim() ?: ""
        val password = binding.etEmployeePassword.text?.toString()?.trim() ?: ""
        val role = binding.actvEmployeeRole.text?.toString()?.trim() ?: ""
        val avatarUrl = binding.etEmployeeAvatarUrl.text?.toString()?.trim() ?: ""

        Log.d("AddEmployeeDialogFragment", "Password value: '$password'")

        if (username.isEmpty() || email.isEmpty() || role.isEmpty() || avatarUrl.isEmpty()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
            Log.w("AddEmployeeDialogFragment", "Dữ liệu nhập vào không đầy đủ.")
            return
        }

        if (userToEdit == null && password.isEmpty()) {
            Toast.makeText(context, "Mật khẩu không được để trống khi thêm mới.", Toast.LENGTH_SHORT).show()
            Log.w("AddEmployeeDialogFragment", "Mật khẩu rỗng khi thêm mới.")
            return
        }

        val userToSave = User(
            uid = userToEdit?.uid ?: "",
            username = username,
            email = email,
            role = role,
            avatar = avatarUrl
        )

        // Sửa lỗi: Truyền null cho mật khẩu khi chỉnh sửa, để ViewModel biết không cần cập nhật mật khẩu.
        // Chỉ truyền mật khẩu khi thêm mới nhân viên.
        val passwordToPass = if (userToEdit == null) password else null
        sharedViewModel.setEmployeeEvent(userToSave, passwordToPass)

        Log.d("AddEmployeeDialogFragment", "Đã truyền dữ liệu thành công. Mật khẩu: $passwordToPass")
        Log.d("AddEmployeeDialogFragment", "Đã truyền sự kiện đến ViewModel.")
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
