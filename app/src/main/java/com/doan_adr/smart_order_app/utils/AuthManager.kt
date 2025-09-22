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
                email = userQuery.documents.first().getString("email")
            } catch (e: Exception) {
                Log.e("AuthManager", "Lỗi khi tìm kiếm username trong Firestore: ${e.message}", e)
                return null
            }
        }

        if (email == null) {
            return null
        }

        return try {
            val authResult: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null
            val userDoc = db.collection("users").document(uid).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi đăng nhập Firebase: ${e.message}", e)
            null
        }
    }

    /**
     * Tạo một người dùng mới trong Firebase Authentication.
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
     * Lấy thông tin người dùng hiện tại, bao gồm cả các trường tùy chỉnh từ Firestore.
     *
     * @return Đối tượng User đầy đủ thông tin, hoặc null nếu không có người dùng đăng nhập.
     */
    suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return try {
            val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi khi lấy dữ liệu người dùng từ Firestore: ${e.message}", e)
            null
        }
    }

    /**
     * Đăng xuất người dùng hiện tại.
     */
    fun signOut() {
        auth.signOut()
    }
}