package com.dacs3.smartmoney.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Add : Screen("add", "Thêm", Icons.Default.Add)
    object Edit : Screen("edit/{transactionId}", "Sửa")
    object Stats : Screen("stats", "Thống kê", Icons.Default.PieChart)
    object Budget : Screen("budget", "Ngân sách", Icons.Default.AccountBalanceWallet)
    object CategoryManagement : Screen("category_management", "Danh mục", Icons.Default.Category)
    object Settings : Screen("settings", "Cài đặt", Icons.Default.Settings)
    object Profile : Screen("profile", "Hồ sơ", Icons.Default.Person)
}
