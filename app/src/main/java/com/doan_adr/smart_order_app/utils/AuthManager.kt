package com.doan_adr.smart_order_app.utils

import android.util.Log
import com.doan_adr.smart_order_app.Models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Đăng nhập người dùng bằng email hoặc username.
     *
     * @param usernameOrEmail Email hoặc username của người dùng.
     * @param password Mật khẩu của người dùng.
     * @return Đối tượng User nếu đăng nhập thành công, ngược lại là null.
     */
    suspend fun login(usernameOrEmail: String, password: String): User? {
        val isEmail = usernameOrEmail.contains("@")
        val email: String?

        if (isEmail) {
            email = usernameOrEmail
        } else {
            try {
                val userQuery = db.collection("users")
                    .whereEqualTo("username", usernameOrEmail)
                    .get()
                    .await()
                if (userQuery.isEmpty) {
                    Log.e("AuthManager", "Không tìm thấy người dùng với username: $usernameOrEmail")
                    return null
                }
                email = userQuery.documents[0].getString("email")
            } catch (e: Exception) {
                Log.e("AuthManager", "Lỗi khi tìm kiếm người dùng: ${e.message}", e)
                return null
            }
        }

        if (email == null) return null

        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                Log.d("AuthManager", "Trạng thái isAccountEnabled: ${user?.isAccountEnabled}")

                // THÊM LOGIC KIỂM TRA MỚI Ở ĐÂY
                if (user != null && user.isAccountEnabled) {
                    user
                } else {
                    Log.d("AuthManager", "Tài khoản bị vô hiệu hóa hoặc không tồn tại.")
                    auth.signOut()
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Đăng nhập thất bại: ${e.message}", e)
            null
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại, bao gồm cả các trường tùy chỉnh từ Firestore.
     *
     * @return Đối tượng User đầy đủ thông tin, hoặc null nếu không có người dùng đăng nhập.
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
            // THÊM LOGIC KIỂM TRA MỚI Ở ĐÂY
            if (user != null && user.isAccountEnabled) {
                user
            } else {
                Log.d("AuthManager", "Tài khoản bị vô hiệu hóa. Đăng xuất ngay lập tức.")
                auth.signOut()
                null
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi khi lấy dữ liệu người dùng từ Firestore: ${e.message}", e)
            auth.signOut()
            null
        }
    }

    /**
     * Tạo tài khoản người dùng mới trong Firebase Authentication.
     *
     * @param email Email của người dùng.
     * @param password Mật khẩu của người dùng.
     * @return UID của người dùng mới nếu thành công, ngược lại là null.
     */
    suspend fun createFirebaseUser(email: String, password: String): String? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.uid
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.w("AuthManager", "Tài khoản $email đã tồn tại.")
            val user = auth.signInWithEmailAndPassword(email, password).await().user
            user?.uid
        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi khi tạo tài khoản: ${e.message}", e)
            null
        }
    }

    /**
     * Đăng xuất người dùng hiện tại khỏi Firebase Auth.
     */
    fun signOut() {
        auth.signOut()
        Log.d("AuthManager", "Người dùng đã đăng xuất.")
    }
}
