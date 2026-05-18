package com.dacs3.smartmoney.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Lịch sử", Icons.Default.History)
    object Add : Screen("add", "Thêm", Icons.Default.Add)
    object Edit : Screen("edit/{transactionId}", "Sửa")
    object Stats : Screen("stats", "Thống kê", Icons.Default.PieChart)
    object Budget : Screen("budget", "Ngân sách", Icons.Default.History)
    object CategoryManagement : Screen("category_management", "Quản lý danh mục", Icons.Default.List)
}
