package com.dacs3.smartmoney.data.model

import com.google.firebase.firestore.DocumentId

data class Transaction(
    @DocumentId val transactionId: String = "", // Đổi tên từ 'id' thành 'transactionId' để tránh xung đột
    val amount: Double = 0.0,
    val categoryName: String = "",
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val type: String = "EXPENSE" // "EXPENSE" hoặc "INCOME"
)