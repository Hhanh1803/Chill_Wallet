package com.dacs3.smartmoney.data.model

import com.google.firebase.firestore.DocumentId

data class GroupFund(
    @DocumentId val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val inviteCode: String = "",
    val adminUid: String = "",
    val memberUids: List<String> = emptyList(),
    val balance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
