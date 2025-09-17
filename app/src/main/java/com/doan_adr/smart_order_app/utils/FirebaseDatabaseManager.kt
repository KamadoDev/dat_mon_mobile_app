package com.doan_adr.smart_order_app.utils

import android.util.Log
import com.doan_adr.smart_order_app.Models.*
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseManager {
    private val db = FirebaseFirestore.getInstance()

    // Biến để lưu trữ cache trong bộ nhớ
    private var cachedDishes: List<Dish>? = null
    private var cachedCategories: List<Category>? = null
    private val cachedToppings = mutableMapOf<String, List<Topping>>()

    suspend fun createMockData() {
        Log.d("FirebaseDatabaseManager", "Đang tạo dữ liệu mẫu...")

        // Bước 1: Thêm categories để lấy IDs
        val categoryData = createMockCategories()
        val categoryIds = addCollectionDataWithIdReturn("categories", categoryData)
        if (categoryIds.isEmpty()) {
            Log.e("FirebaseDatabaseManager", "Lỗi: Không thể tạo categories.")
            return
        }

        // Bước 2: Thêm toppings để lấy IDs
        val toppingData = createMockToppings()
        val toppingIds = addCollectionDataWithIdReturn("toppings", toppingData)
        if (toppingIds.isEmpty()) {
            Log.e("FirebaseDatabaseManager", "Lỗi: Không thể tạo toppings.")
            return
        }

        // Bước 3: Dùng IDs của categories và toppings để tạo dishes
        val dishes = createMockDishes(categoryIds, toppingIds)
        addCollectionData("dishes", dishes)

        // Các bước còn lại
        val tables = createMockTables()
        val discounts = createMockDiscounts()
        addCollectionData("tables", tables)
        addCollectionData("discounts", discounts)

        Log.d("FirebaseDatabaseManager", "Tạo dữ liệu mẫu hoàn tất.")
    }

    private suspend fun addCollectionData(collectionPath: String, dataList: List<Map<String, Any?>>) {
        val batch = db.batch()
        dataList.forEach { data ->
            val docRef = db.collection(collectionPath).document()
            batch.set(docRef, data)
        }
        try {
            batch.commit().await()
            Log.d("FirebaseDatabaseManager", "Đã thêm dữ liệu vào collection '$collectionPath' thành công.")
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi thêm dữ liệu vào '$collectionPath': ${e.message}")
        }
    }

    private suspend fun addCollectionDataWithIdReturn(collectionPath: String, dataList: List<Map<String, Any?>>): Map<String, String> {
        val batch = db.batch()
        val newIds = mutableMapOf<String, String>()
        val collectionRef = db.collection(collectionPath)
        dataList.forEach { data ->
            val docRef = collectionRef.document()
            batch.set(docRef, data)
            newIds[data["name"] as String] = docRef.id
        }
        try {
            batch.commit().await()
            Log.d("FirebaseDatabaseManager", "Đã thêm dữ liệu vào collection '$collectionPath' thành công.")
            return newIds
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi thêm dữ liệu vào '$collectionPath': ${e.message}")
            return emptyMap()
        }
    }

    // TABLE
    private fun createMockTables(): List<Map<String, Any?>> {
        return (1..10).map { i ->
            val status = if (i <= 7) "available" else "occupied"
            Table(name = "Bàn $i", status = status, tableNumber = i, currentOrderId = null).toMap()
        }
    }

    suspend fun lockTable(tableId: String) {
        try {
            db.collection("tables").document(tableId)
                .update("status", "occupied")
                .await()
        } catch (e: Exception) {
            throw Exception("Lỗi khi khóa bàn: ${e.message}", e)
        }
    }

    suspend fun unlockTable(tableId: String) {
        try {
            db.collection("tables").document(tableId)
                .update("status", "available", "currentOrderId", null)
                .await()
            Log.d("FirebaseDatabaseManager", "Đã mở khóa bàn: $tableId")
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi mở khóa bàn: ${e.message}", e)
            throw Exception("Lỗi khi mở khóa bàn: ${e.message}", e)
        }
    }

    fun addTablesListener(onTablesChanged: (List<Table>) -> Unit): ListenerRegistration {
        return db.collection("tables")
            .orderBy("tableNumber")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseDBManager", "Lỗi lắng nghe bàn: ", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tables = snapshot.documents.mapNotNull { doc ->
                        val table = doc.toObject(Table::class.java)?.copy(id = doc.id)
                        table
                    }
                    onTablesChanged(tables)
                }
            }
    }

    // DISHES
    private fun createMockDishes(categoryIds: Map<String, String>, toppingIds: Map<String, String>): List<Map<String, Any?>> {
        return listOf(
            Dish(
                name = "Phở Bò", description = "Món phở truyền thống.",
                originalPrice = 45000.0, discountedPrice = 40000.0,
                categoryId = categoryIds["Món chính"] ?: "",
                imageUrl = "https://media.istockphoto.com/id/1462352351/vi/anh/ph%E1%BB%9F.jpg?s=612x612&w=0&k=20&c=H8CFdkpTEMIHCrtByEkhpW0um8IPmjPVyeHKYpLyoVc=",
                healthTips = "Ăn kèm rau sống.", toppingsAvailable = true,
                toppingIds = listOf(toppingIds["Trứng"] ?: "", toppingIds["Thịt Bò Thêm"] ?: "")
            ).toMap(),
            Dish(
                name = "Bún Chả", description = "Bún chả Hà Nội.",
                originalPrice = 40000.0, discountedPrice = 40000.0,
                categoryId = categoryIds["Món chính"] ?: "",
                imageUrl = "https://tiki.vn/blog/wp-content/uploads/2023/03/UOgFsuKHUofRVdLVJGRkTsnx-dRAt2qzIFgBjMYfGq35_AGu-vX9GhFR7K0EoHfG9VUQPkBdnHx63QPiSFbhUK1uNTVO40JVFpFZ9h_SKPzPITkg6cuybmWNjPjIFB9MCDElqoaovdLaV5stjw5i_D8.png",
                healthTips = "Giàu đạm và vitamin.", toppingsAvailable = true,
                toppingIds = listOf(toppingIds["Trứng"] ?: "")
            ).toMap(),
            Dish(
                name = "Nước Cam Ép", description = "Thức uống giải khát.",
                originalPrice = 20000.0, discountedPrice = 20000.0,
                categoryId = categoryIds["Thức uống"] ?: "",
                imageUrl = "https://suckhoedoisong.qltns.mediacdn.vn/324455921873985536/2022/2/19/cach-lam-nuoc-cam-ep-ngon-va-thom-ket-hop-voi-le-va-gung-5-1645248090817401855254.jpg",
                healthTips = "Bổ sung vitamin C.", toppingsAvailable = false, toppingIds = emptyList()
            ).toMap(),
            Dish(
                name = "Kem Vani", description = "Kem mát lạnh.",
                originalPrice = 25000.0, discountedPrice = 25000.0,
                categoryId = categoryIds["Tráng miệng"] ?: "",
                imageUrl = "https://media.istockphoto.com/id/1326143969/vi/anh/b%C3%A1t-v%E1%BB%9Bi-nh%E1%BB%AFng-qu%E1%BA%A3-b%C3%B3ng-kem-vani.jpg?s=612x612&w=0&k=20&c=JCMWRU9k-2TOkSc-1kyC_J0qD0yJzFtVsO0_vLjtvVE=",
                healthTips = "Món tráng miệng.", toppingsAvailable = false, toppingIds = emptyList()
            ).toMap()
        )
    }

    suspend fun getDishesByCategory(categoryId: String): List<Dish> {
        if (cachedDishes != null) {
            Log.d("FirebaseDatabaseManager", "Trả về món ăn từ cache.")
            return if (categoryId == "all") cachedDishes!! else cachedDishes!!.filter { it.categoryId == categoryId }
        }

        return try {
            val snapshot = db.collection("dishes").get().await()
            val dishes = snapshot.documents.mapNotNull { doc -> doc.toObject(Dish::class.java)?.copy(id = doc.id) }
            cachedDishes = dishes
            if (categoryId == "all") dishes else dishes.filter { it.categoryId == categoryId }
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi lấy món ăn: ${e.message}")
            emptyList()
        }
    }

    fun addDishesListener(onDishesChanged: (List<Dish>) -> Unit): ListenerRegistration {
        return db.collection("dishes")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseDBManager", "Lỗi lắng nghe món ăn: ", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val dishes = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Dish::class.java)?.copy(id = doc.id)
                    }
                    onDishesChanged(dishes)
                }
            }
    }

    // TOPPINGS
    private fun createMockToppings(): List<Map<String, Any?>> {
        return listOf(
            Topping(name = "Trứng", price = 5000.0).toMap(),
            Topping(name = "Thịt Bò Thêm", price = 15000.0).toMap()
        )
    }

    suspend fun getToppingsForDish(dish: Dish): List<Topping> {
        val toppingIds = dish.toppingIds
        if (toppingIds.isEmpty()) {
            Log.d("FirebaseDatabaseManager", "Không có toppings nào được liên kết với món ăn: ${dish.name}")
            return emptyList()
        }

        if (cachedToppings.containsKey(dish.id)) {
            Log.d("FirebaseDatabaseManager", "Trả về toppings từ cache cho món ăn ${dish.name}")
            return cachedToppings[dish.id]!!
        }

        return try {
            val snapshot = db.collection("toppings")
                .whereIn(FieldPath.documentId(), toppingIds)
                .get()
                .await()
            val toppings = snapshot.documents.mapNotNull { doc -> doc.toObject(Topping::class.java)?.copy(id = doc.id) }
            cachedToppings[dish.id] = toppings
            Log.d("FirebaseDatabaseManager", "Đã tải thành công ${toppings.size} toppings cho món ăn ${dish.name}")
            toppings
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi lấy topping cho món ăn ${dish.name}: ${e.message}")
            emptyList()
        }
    }

    fun addToppingsListener(toppingIds: List<String>, onToppingsChanged: (List<Topping>) -> Unit): ListenerRegistration {
        // 1. Kiểm tra cache trước
        val cacheKey = toppingIds.sorted().joinToString(",")
        if (cachedToppings.containsKey(cacheKey)) {
            Log.d("FirebaseDatabaseManager", "Trả về topping từ cache.")
            onToppingsChanged(cachedToppings[cacheKey]!!)
            // Vẫn lắng nghe real-time cho các lần sau nếu dữ liệu thay đổi
        }

        // 2. Lắng nghe real-time từ Firestore
        return db.collection("toppings")
            .whereIn(FieldPath.documentId(), toppingIds)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseDBManager", "Lỗi lắng nghe topping: ", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val toppings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Topping::class.java)?.copy(id = doc.id)
                    }
                    // 3. Cập nhật cache và gọi callback
                    cachedToppings[cacheKey] = toppings
                    onToppingsChanged(toppings)
                    Log.d("FirebaseDatabaseManager", "Toppings được cập nhật từ Firestore.")
                }
            }
    }

    // CATEGORIES
    private fun createMockDiscounts(): List<Map<String, Any?>> {
        return listOf(
            Discount(
                code = "GIAM10", discountType = "fixed", value = 10000.0,
                minOrderValue = 100000.0, validUntil = "2025-12-31"
            ).toMap(),
            Discount(
                code = "SALE20", discountType = "percentage", value = 0.2,
                minOrderValue = 200000.0, validUntil = "2025-11-30"
            ).toMap()
        )
    }

    // Thêm hàm này vào class FirebaseDatabaseManager
    suspend fun getDiscountByCode(code: String): Discount? {
        return try {
            val snapshot = db.collection("discounts")
                .whereEqualTo("code", code)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject(Discount::class.java)
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi lấy mã giảm giá: ${e.message}")
            null
        }
    }

    private fun createMockCategories(): List<Map<String, Any?>> {
        return listOf(
            Category(name = "Món chính", imageUrl = "https://example.com/main_icon.png").toMap(),
            Category(name = "Thức uống", imageUrl = "https://example.com/drink_icon.png").toMap(),
            Category(name = "Tráng miệng", imageUrl = "https://example.com/dessert_icon.png").toMap(),
        )
    }

    suspend fun getCategories(): List<Category> {
        if (cachedCategories != null) {
            Log.d("FirebaseDatabaseManager", "Trả về danh mục từ cache.")
            return cachedCategories!!
        }
        return try {
            val snapshot = db.collection("categories").get().await()
            val categories = snapshot.documents.mapNotNull { doc -> doc.toObject(Category::class.java)?.copy(id = doc.id) }
            cachedCategories = categories
            categories
        } catch (e: Exception) {
            Log.e("FirebaseDatabaseManager", "Lỗi khi lấy danh mục: ${e.message}")
            emptyList()
        }
    }

    fun addCategoriesListener(onCategoriesChanged: (List<Category>) -> Unit): ListenerRegistration {
        return db.collection("categories")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("FirebaseDBManager", "Lỗi lắng nghe danh mục: ", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val categories = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    }
                    onCategoriesChanged(categories)
                }
            }
    }

    suspend fun createOrder(order: Order) {
        // Lưu đơn hàng vào collection "orders"
        db.collection("orders").document(order.id).set(order.toMap()).await()
        Log.d("FirebaseDatabaseManager", "Đơn hàng ${order.id} đã được tạo thành công.")

        // Đánh dấu bàn đã được đặt hàng là "busy"
        db.collection("tables").document(order.tableId).update("status", "busy").await()
        Log.d("FirebaseDatabaseManager", "Đã cập nhật trạng thái bàn ${order.tableId} thành 'busy'.")
    }

}