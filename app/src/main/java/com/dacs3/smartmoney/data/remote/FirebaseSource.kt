package com.dacs3.smartmoney.data.remote

import com.dacs3.smartmoney.data.model.User
import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.data.model.Budget
import com.dacs3.smartmoney.data.model.Category
import com.dacs3.smartmoney.data.model.GroupFund
import com.dacs3.smartmoney.data.model.GroupTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSource {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun initializeUserIfNeeded(user: com.google.firebase.auth.FirebaseUser): Result<Boolean> {
        val uid = user.uid
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                val userData = hashMapOf(
                    "uid" to uid,
                    "displayName" to (user.displayName ?: "Người dùng mới"),
                    "fullName" to (user.displayName ?: "Người dùng mới"),
                    "email" to (user.email ?: ""),
                    "role" to "USER",
                    "isLocked" to false,
                    "joinDate" to System.currentTimeMillis()
                )
                db.collection("users").document(uid).set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getUid(): String? {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            android.util.Log.w("FirebaseSource", "getUid: No current user authenticated")
        }
        return uid
    }

    // --- CHỨC NĂNG QUẢN TRỊ (ADMIN) ---

    fun getAllUsersRealtime(): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for all users")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to users: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        try {
                            val data = doc.data
                            val rawJoinDate = data?.get("joinDate")
                            val joinDateVal = when (rawJoinDate) {
                                is Long -> rawJoinDate
                                is String -> rawJoinDate.toLongOrNull() ?: 0L
                                is Number -> rawJoinDate.toLong()
                                is com.google.firebase.Timestamp -> rawJoinDate.seconds * 1000
                                else -> 0L
                            }
                            
                            val displayName = data?.get("displayName") as? String ?: ""
                            val fullName = data?.get("fullName") as? String ?: ""
                            val email = data?.get("email") as? String ?: ""
                            
                            User(
                                uid = doc.id,
                                displayName = if (displayName.isBlank() && fullName.isBlank()) "ID: ${doc.id.take(8)}" else displayName,
                                fullName = fullName,
                                email = if (email.isBlank()) "No Email" else email,
                                role = data?.get("role") as? String ?: "USER",
                                isLocked = data?.get("isLocked") as? Boolean ?: false,
                                joinDate = joinDateVal
                            )
                        } catch (e: Exception) {
                            User(uid = doc.id, displayName = "Lỗi dữ liệu: ${doc.id.take(5)}", email = e.message ?: "Unknown error")
                        }
                    }
                    trySend(items.sortedByDescending { it.joinDate })
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateUserLockStatus(uid: String, isLocked: Boolean): Result<Boolean> {
        return try {
            db.collection("users").document(uid).update("isLocked", isLocked).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(uid: String, newRole: String): Result<Boolean> {
        return try {
            db.collection("users").document(uid).update("role", newRole).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(uid: String): Result<Boolean> {
        return try {
            db.collection("users").document(uid).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGlobalStats(): Result<Map<String, Any>> {
        return try {
            val usersSnapshot = db.collection("users").get().await()
            val usersCount = usersSnapshot.size()
            val groupsSnapshot = db.collection("groups").get().await()
            val groupsCount = groupsSnapshot.size()
            
            // This requires the 'ADMIN' role and specific rules for collectionGroup
            val allTransactions = db.collectionGroup("transactions").get().await()
            val totalTransactions = allTransactions.size()
            
            var totalIncome = 0.0
            var totalExpense = 0.0
            val categoryCounts = mutableMapOf<String, Int>()
            
            allTransactions.documents.forEach { doc ->
                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                val type = doc.getString("type") ?: "EXPENSE"
                val catName = doc.getString("categoryName")
                
                if (type == "INCOME") totalIncome += amount else totalExpense += amount
                
                if (!catName.isNullOrEmpty()) {
                    categoryCounts[catName] = categoryCounts.getOrDefault(catName, 0) + 1
                }
            }
            
            val mostUsedCategory = categoryCounts.maxByOrNull { it.value }?.key ?: "Chưa có"
            val mostUsedCategoryCount = categoryCounts.maxByOrNull { it.value }?.value ?: 0
            
            val totalGroupBalance = groupsSnapshot.documents.sumOf { (it.get("balance") as? Number)?.toDouble() ?: 0.0 }
            
            Result.success(mapOf(
                "totalUsers" to usersCount,
                "totalGroups" to groupsCount,
                "totalTransactions" to totalTransactions,
                "mostUsedCategory" to mostUsedCategory,
                "mostUsedCategoryCount" to mostUsedCategoryCount,
                "totalIncome" to totalIncome,
                "totalExpense" to totalExpense,
                "totalGroupBalance" to totalGroupBalance
            ))
        } catch (e: Exception) {
            android.util.Log.e("FirebaseSource", "Error getting global stats: ${e.message}")
            Result.failure(e)
        }
    }

    fun getCombinedCategoriesRealtime(): Flow<List<Category>> = callbackFlow {
        val uid = getUid() ?: run { 
            trySend(emptyList())
            close()
            return@callbackFlow 
        }
        
        val globalRef = db.collection("global_categories")
        val userRef = db.collection("users").document(uid).collection("categories")

        var globals = emptyList<Category>()
        var locals = emptyList<Category>()

        fun updateCombined() {
            val combined = (globals + locals).distinctBy { it.name }
            trySend(combined)
        }

        val globalListener = globalRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("FirebaseSource", "Permission denied for global categories")
                } else {
                    android.util.Log.e("FirebaseSource", "Error listening to global categories: ${error.message}")
                }
                updateCombined()
                return@addSnapshotListener
            }
            globals = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            updateCombined()
        }

        val userListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    android.util.Log.w("FirebaseSource", "Permission denied for user categories")
                } else {
                    android.util.Log.e("FirebaseSource", "Error listening to user categories: ${error.message}")
                }
                updateCombined()
                return@addSnapshotListener
            }
            locals = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            updateCombined()
        }

        awaitClose {
            globalListener.remove()
            userListener.remove()
        }
    }

    fun getGlobalCategoriesRealtime(): Flow<List<Category>> = callbackFlow {
        val subscription = db.collection("global_categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for global categories")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to global categories: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addGlobalCategory(category: Category): Result<Boolean> {
        return try {
            db.collection("global_categories").add(category).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGlobalCategory(category: Category): Result<Boolean> {
        if (category.id.isEmpty()) return Result.failure(Exception("ID không hợp lệ"))
        return try {
            db.collection("global_categories").document(category.id).set(category).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGlobalCategory(id: String): Result<Boolean> {
        return try {
            db.collection("global_categories").document(id).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTransaction(transaction: Transaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).collection("transactions").add(transaction).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllTransactionsRealtime(): Flow<List<Transaction>> = callbackFlow {
        val uid = getUid() ?: run { 
            trySend(emptyList())
            close()
            return@callbackFlow 
        }
        val subscription = db.collection("users").document(uid)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for transactions")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to transactions: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Transaction::class.java)?.copy(transactionId = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseSource", "Error parsing transaction ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteTransaction(transactionId: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).collection("transactions").document(transactionId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).collection("transactions").document(transaction.transactionId).set(transaction).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBudgetsRealtime(): Flow<List<Budget>> = callbackFlow {
        val uid = getUid() ?: run { 
            trySend(emptyList())
            close()
            return@callbackFlow 
        }
        val subscription = db.collection("users").document(uid).collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for budgets")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to budgets: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Budget::class.java)?.copy(budgetId = doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseSource", "Error parsing budget ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveBudget(budget: Budget): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            if (budget.budgetId.isEmpty()) {
                db.collection("users").document(uid).collection("budgets").add(budget).await()
            } else {
                db.collection("users").document(uid).collection("budgets").document(budget.budgetId).set(budget).await()
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCategoriesRealtime(): Flow<List<Category>> = callbackFlow {
        val uid = getUid() ?: run { 
            trySend(emptyList())
            close()
            return@callbackFlow 
        }
        val subscription = db.collection("users").document(uid).collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for categories")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to categories: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addCategory(category: Category): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).collection("categories").add(category).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid).collection("categories").document(categoryId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createGroupFund(name: String, description: String): Result<String> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val inviteCode = (100000..999999).random().toString()
        val group = GroupFund(name = name, description = description, inviteCode = inviteCode, adminUid = uid, memberUids = listOf(uid), balance = 0.0)
        return try {
            db.collection("groups").add(group).await()
            Result.success(inviteCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroupFund(inviteCode: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val query = db.collection("groups").whereEqualTo("inviteCode", inviteCode).limit(1).get().await()
            if (query.isEmpty) return Result.failure(Exception("Mã mời không tồn tại"))
            val doc = query.documents[0]
            val group = doc.toObject(GroupFund::class.java)
            if (group != null) {
                if (group.memberUids.contains(uid)) return Result.failure(Exception("Bạn đã tham gia nhóm này rồi"))
                val newMembers = group.memberUids.toMutableList().apply { add(uid) }
                db.collection("groups").document(doc.id).update("memberUids", newMembers).await()
                Result.success(true)
            } else Result.failure(Exception("Lỗi dữ liệu nhóm"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getJoinedGroupsRealtime(): Flow<List<GroupFund>> = callbackFlow {
        val uid = getUid() ?: run { 
            trySend(emptyList())
            close()
            return@callbackFlow 
        }
        val subscription = db.collection("groups").whereArrayContains("memberUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for joined groups")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to joined groups: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(GroupFund::class.java)?.copy(groupId = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    fun getGroupRealtime(groupId: String): Flow<GroupFund?> = callbackFlow {
        val subscription = db.collection("groups").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for group $groupId")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to group $groupId: ${error.message}")
                    }
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(GroupFund::class.java)?.copy(groupId = snapshot.id))
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getGroupTransactionsRealtime(groupId: String): Flow<List<GroupTransaction>> = callbackFlow {
        val subscription = db.collection("groups").document(groupId).collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for group transactions in $groupId")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to group transactions: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    try {
                        val data = doc.data
                        val rawDate = data?.get("date")
                        val dateVal = when (rawDate) {
                            is Long -> rawDate
                            is Number -> rawDate.toLong()
                            is com.google.firebase.Timestamp -> rawDate.seconds * 1000
                            is String -> rawDate.toLongOrNull() ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                        }
                        
                        GroupTransaction(
                            transactionId = doc.id,
                            amount = (data?.get("amount") as? Number)?.toDouble() ?: 0.0,
                            categoryName = data?.get("categoryName") as? String ?: "Khác",
                            date = dateVal,
                            note = data?.get("note") as? String ?: "",
                            type = data?.get("type") as? String ?: "EXPENSE",
                            createdBy = data?.get("createdBy") as? String ?: "",
                            creatorName = data?.get("creatorName") as? String ?: "Thành viên",
                            creatorPhotoUrl = data?.get("creatorPhotoUrl") as? String ?: ""
                        )
                    } catch (e: Exception) {
                        GroupTransaction(transactionId = doc.id, note = "Lỗi dữ liệu", date = System.currentTimeMillis())
                    }
                } ?: emptyList()
                trySend(items.sortedByDescending { it.date })
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addGroupTransaction(groupId: String, transaction: GroupTransaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val userDoc = try { db.collection("users").document(uid).get().await() } catch (e: Exception) { null }
        val userName = userDoc?.getString("displayName") ?: userDoc?.getString("fullName") ?: "Thành viên"
        val userPhoto = userDoc?.getString("photoUrl") ?: ""
        val finalTransaction = transaction.copy(createdBy = uid, creatorName = userName, creatorPhotoUrl = userPhoto)
        
        return try {
            db.runTransaction { firestoreTransaction ->
                val groupRef = db.collection("groups").document(groupId)
                val groupSnap = firestoreTransaction.get(groupRef)
                val currentBalance = groupSnap.getDouble("balance") ?: 0.0
                val newBalance = if (transaction.type == "INCOME") currentBalance + transaction.amount else currentBalance - transaction.amount
                firestoreTransaction.update(groupRef, "balance", newBalance)
                val transRef = db.collection("groups").document(groupId).collection("transactions").document()
                firestoreTransaction.set(transRef, finalTransaction)
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGroupMembersRealtime(memberUids: List<String>): Flow<List<User>> = callbackFlow {
        if (memberUids.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), memberUids)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for group members")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to group members: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.toObjects(User::class.java)
                    trySend(users)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getAllGroupsRealtime(): Flow<List<com.dacs3.smartmoney.data.model.GroupFund>> = callbackFlow {
        val subscription = db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        android.util.Log.w("FirebaseSource", "Permission denied for all groups (Admin check)")
                    } else {
                        android.util.Log.e("FirebaseSource", "Error listening to all groups: ${error.message}")
                    }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(com.dacs3.smartmoney.data.model.GroupFund::class.java)?.copy(groupId = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteGroupFund(groupId: String): Result<Boolean> {
        return try {
            // Xóa tất cả giao dịch trong nhóm trước (optional nhưng tốt hơn)
            val transactions = db.collection("groups").document(groupId).collection("transactions").get().await()
            db.runTransaction { transaction ->
                transactions.documents.forEach { transaction.delete(it.reference) }
                transaction.delete(db.collection("groups").document(groupId))
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
