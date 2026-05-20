package com.dacs3.smartmoney.data.model

import com.google.firebase.firestore.DocumentId

data class GroupTransaction(
    @DocumentId val transactionId: String = "",
    val amount: Double = 0.0,
    val categoryName: String = "",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val type: String = "EXPENSE", // "EXPENSE" hoặc "INCOME"
    val createdBy: String = "", // UID người tạo giao dịch
    val creatorName: String = "", // Tên người tạo để hiển thị nhanh
    val creatorPhotoUrl: String = "" // Ảnh người tạo
)
