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

    suspend fun login(usernameOrEmail: String, password: String): User? {
        // Bước 1: Xác định xem input là email hay username
        val isEmail = usernameOrEmail.contains("@")
        val email: String?

        if (isEmail) {
            // Trường hợp 1: Input là một email hợp lệ
            email = usernameOrEmail
        } else {
            // Trường hợp 2: Input là username, cần tìm email từ Firestore
            try {
                val userQuery = db.collection("users")
                    .whereEqualTo("username", usernameOrEmail)
                    .get()
                    .await()

                if (userQuery.isEmpty) {
                    // Không tìm thấy người dùng với username này
                    Log.e("AuthManager", "Không tìm thấy người dùng với username: $usernameOrEmail")
                    return null
                }
                // Lấy email từ document đầu tiên tìm thấy
                email = userQuery.documents.first().getString("email")
            } catch (e: Exception) {
                Log.e("AuthManager", "Lỗi khi tìm kiếm username trong Firestore: ${e.message}", e)
                return null
            }
        }

        // Bước 2: Dùng email và password để đăng nhập bằng Firebase Authentication
        // Nếu email rỗng, thoát khỏi hàm
        if (email == null) {
            return null
        }

        return try {
            val authResult: AuthResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return null

            // Sau khi đăng nhập thành công, lấy dữ liệu user từ Firestore
            val userDoc = db.collection("users").document(uid).get().await()
            // toObject sẽ tự động ánh xạ profilePictureUrl từ Firestore sang lớp User
            userDoc.toObject(User::class.java)

        } catch (e: Exception) {
            Log.e("AuthManager", "Lỗi đăng nhập Firebase: ${e.message}", e)
            null
        }
    }

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

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        // Để lấy được profilePictureUrl, bạn cần fetch từ Firestore
        // Tùy chọn 1: Trả về một đối tượng User tạm thời (chưa có avatar)
        // Đây là cách hiện tại của bạn
        return User(
            uid = firebaseUser.uid,
            username = firebaseUser.displayName ?: "Đầu bếp",
            email = firebaseUser.email ?: "",
            avatar = "" // Đặt avatar rỗng tạm thời
        )

        // Tùy chọn 2: Viết một hàm `suspend` để fetch đầy đủ từ Firestore
        /*
        suspend fun getCurrentUserFromFirestore(): User? {
            val firebaseUser = auth.currentUser ?: return null
            return try {
                val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                userDoc.toObject(User::class.java)
            } catch (e: Exception) {
                Log.e("AuthManager", "Lỗi khi lấy dữ liệu người dùng từ Firestore: ${e.message}", e)
                null
            }
        }
        */
    }

    fun signOut() {
        auth.signOut()
    }
}