package com.doan_adr.smart_order_app.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.doan_adr.smart_order_app.Models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Hàm đăng nhập với email và mật khẩu.
     * @return User object nếu đăng nhập thành công, ngược lại trả về null.
     */
    suspend fun login(email: String, password: String): User? {
        return try {
            val authResult: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null

            val userDoc = db.collection("users").document(uid).get().await()
            userDoc.toObject(User::class.java)

        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi đăng nhập: ${e.message}", e)
            null
        }
    }

    /**
     * Tạo một tài khoản mock cho Đầu bếp hoặc Quản lý.
     */
    /**
     * Tạo một người dùng trong Firebase Authentication.
     * @return UID của người dùng nếu thành công, ngược lại trả về null.
     */
    suspend fun createFirebaseUser(email: String, password: String): String? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.uid
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.w("AuthManager", "Tài khoản $email đã tồn tại.")
            // Lấy UID của tài khoản đã tồn tại
            val user = auth.signInWithEmailAndPassword(email, password).await().user
            user?.uid
        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi khi tạo tài khoản: ${e.message}", e)
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
}