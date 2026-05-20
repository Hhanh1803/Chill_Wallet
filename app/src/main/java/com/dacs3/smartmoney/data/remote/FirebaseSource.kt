package com.dacs3.smartmoney.data.remote

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

    // Lấy UID người dùng hiện tại để thực hiện nhiệm vụ xác thực
    private fun getUid(): String? = auth.currentUser?.uid

    // 1. CHỨC NĂNG: THÊM KHOẢN THU CHI
    suspend fun addTransaction(transaction: Transaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid)
                .collection("transactions")
                .add(transaction)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. CHỨC NĂNG: ĐỒNG BỘ DỮ LIỆU THỜI GIAN THỰC
    fun getAllTransactionsRealtime(): Flow<List<Transaction>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = db.collection("users").document(uid)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.copy(transactionId = doc.id)
                    }
                    trySend(items)
                }
            }

        awaitClose { subscription.remove() }
    }

    // 3. CHỨC NĂNG: XÓA KHOẢN THU CHI
    suspend fun deleteTransaction(transactionId: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid)
                .collection("transactions").document(transactionId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. CHỨC NĂNG: CẬP NHẬT KHOẢN THU CHI
    suspend fun updateTransaction(transaction: Transaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        if (transaction.transactionId.isEmpty()) return Result.failure(Exception("ID không hợp lệ"))
        
        return try {
            db.collection("users").document(uid)
                .collection("transactions").document(transaction.transactionId)
                .set(transaction)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. CHỨC NĂNG: QUẢN LÝ NGÂN SÁCH (Budget)
    fun getBudgetsRealtime(): Flow<List<Budget>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = db.collection("users").document(uid)
            .collection("budgets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Budget::class.java)?.copy(budgetId = doc.id)
                    }
                    trySend(items)
                }
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

    // 6. CHỨC NĂNG: QUẢN LÝ DANH MỤC (Categories)
    fun getCategoriesRealtime(): Flow<List<Category>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = db.collection("users").document(uid)
            .collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Category::class.java)?.copy(id = doc.id)
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addCategory(category: Category): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid)
                .collection("categories")
                .add(category)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            db.collection("users").document(uid)
                .collection("categories").document(categoryId)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. CHỨC NĂNG: QUẢN LÝ QUỸ NHÓM (Group Fund)
    suspend fun createGroupFund(name: String, description: String): Result<String> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val inviteCode = (100000..999999).random().toString() // Mã mời 6 số ngẫu nhiên
        val group = GroupFund(
            name = name,
            description = description,
            inviteCode = inviteCode,
            adminUid = uid,
            memberUids = listOf(uid),
            balance = 0.0
        )
        return try {
            val docRef = db.collection("groups").add(group).await()
            Result.success(inviteCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroupFund(inviteCode: String): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        return try {
            val query = db.collection("groups")
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .await()
            
            if (query.isEmpty) {
                return Result.failure(Exception("Mã mời không tồn tại"))
            }

            val doc = query.documents[0]
            val group = doc.toObject(GroupFund::class.java)
            if (group != null) {
                if (group.memberUids.contains(uid)) {
                    return Result.failure(Exception("Bạn đã tham gia nhóm này rồi"))
                }
                val newMembers = group.memberUids.toMutableList().apply { add(uid) }
                db.collection("groups").document(doc.id)
                    .update("memberUids", newMembers)
                    .await()
                Result.success(true)
            } else {
                Result.failure(Exception("Lỗi dữ liệu nhóm"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getJoinedGroupsRealtime(): Flow<List<GroupFund>> = callbackFlow {
        val uid = getUid()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val subscription = db.collection("groups")
            .whereArrayContains("memberUids", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(GroupFund::class.java)?.copy(groupId = doc.id)
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getGroupTransactionsRealtime(groupId: String): Flow<List<GroupTransaction>> = callbackFlow {
        val subscription = db.collection("groups").document(groupId)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(GroupTransaction::class.java)?.copy(transactionId = doc.id)
                    }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addGroupTransaction(groupId: String, transaction: GroupTransaction): Result<Boolean> {
        val uid = getUid() ?: return Result.failure(Exception("Chưa đăng nhập"))
        val currentUser = auth.currentUser
        
        // Lấy thông tin từ Firestore
        val userDoc = try { db.collection("users").document(uid).get().await() } catch (e: Exception) { null }
        
        // Ưu tiên: Firestore displayName -> Firestore fullName -> Auth displayName -> "Thành viên"
        val userName = userDoc?.getString("displayName") 
            ?: userDoc?.getString("fullName") 
            ?: currentUser?.displayName 
            ?: "Thành viên"
            
        // Ưu tiên: Firestore photoUrl -> Auth photoUrl -> ""
        val userPhoto = userDoc?.getString("photoUrl") 
            ?: currentUser?.photoUrl?.toString() 
            ?: ""
        
        val finalTransaction = transaction.copy(
            createdBy = uid, 
            creatorName = userName,
            creatorPhotoUrl = userPhoto
        )
        
        return try {
            db.runTransaction { firestoreTransaction ->
                val groupRef = db.collection("groups").document(groupId)
                val groupSnap = firestoreTransaction.get(groupRef)
                val currentBalance = groupSnap.getDouble("balance") ?: 0.0
                
                val newBalance = if (transaction.type == "INCOME") {
                    currentBalance + transaction.amount
                } else {
                    currentBalance - transaction.amount
                }
                
                firestoreTransaction.update(groupRef, "balance", newBalance)
                
                val transRef = db.collection("groups").document(groupId)
                    .collection("transactions").document()
                firestoreTransaction.set(transRef, finalTransaction)
            }.await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
