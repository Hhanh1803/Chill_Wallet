package com.dacs3.smartmoney.data.model

import com.google.firebase.firestore.DocumentId

data class Budget(
    @DocumentId val budgetId: String = "",
    val categoryName: String = "",
    val limitAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val month: Int = 0, // 0-11
    val year: Int = 0
)