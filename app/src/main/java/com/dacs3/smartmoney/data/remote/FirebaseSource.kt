package com.dacs3.smartmoney.data.remote

import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.data.model.Budget
import com.dacs3.smartmoney.data.model.Category
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
}
