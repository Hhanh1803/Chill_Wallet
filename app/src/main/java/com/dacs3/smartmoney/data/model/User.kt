package com.dacs3.smartmoney.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val fullName: String = "", // Thêm để tương thích nếu Firestore dùng tên này
    val email: String = "",
    val role: String = "USER", 
    val isLocked: Boolean = false,
    val joinDate: Long = System.currentTimeMillis()
) {
    // Hàm tiện ích để lấy tên hiển thị tốt nhất
    fun getBestName(): String {
        return when {
            displayName.isNotBlank() -> displayName
            fullName.isNotBlank() -> fullName
            email.isNotBlank() -> email.substringBefore("@")
            else -> "ID: ${uid.take(8)}"
        }
    }
}
