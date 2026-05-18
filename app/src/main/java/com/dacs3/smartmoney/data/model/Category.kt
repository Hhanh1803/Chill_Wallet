package com.dacs3.smartmoney.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val type: String = "EXPENSE", // "EXPENSE" hoặc "INCOME"
    val iconName: String = "Category"
)
