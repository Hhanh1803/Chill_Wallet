package com.dacs3.smartmoney.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {
    // Danh sách danh mục Chi tiêu đầy đủ hơn
    val EXPENSE_CATEGORIES = listOf(
        "Ăn uống", "Di chuyển", "Mua sắm", "Học tập", 
        "Sức khỏe", "Giải trí", "Tiền điện", "Tiền nước", 
        "Internet", "Xăng xe", "Thuê nhà", "Khác"
    )

    // Danh sách danh mục Thu nhập đầy đủ hơn
    val INCOME_CATEGORIES = listOf(
        "Lương", "Thu nhập", "Tiền thưởng", "Kinh doanh", "Khác"
    )

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return format.format(amount)
    }

    fun formatNumber(number: Long): String {
        return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(number)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatShortAmount(amount: Double): String {
        val absAmount = Math.abs(amount)
        val locale = Locale("vi", "VN")
        return when {
            absAmount >= 1_000_000 -> String.format(locale, "%.1fM", amount / 1_000_000)
            absAmount >= 1_000 -> String.format(locale, "%.0fK", amount / 1_000)
            else -> amount.toInt().toString()
        }
    }

    // Hàm lấy Icon tương ứng với tên danh mục (đã bổ sung đầy đủ)
    fun getCategoryIcon(categoryName: String): ImageVector {
        return when (categoryName.lowercase()) {
            "ăn uống" -> Icons.Default.Restaurant
            "di chuyển" -> Icons.Default.DirectionsBus
            "xăng xe" -> Icons.Default.LocalGasStation
            "mua sắm" -> Icons.Default.ShoppingBag
            "lương" -> Icons.Default.Payments
            "thu nhập" -> Icons.Default.AccountBalanceWallet
            "kinh doanh" -> Icons.Default.Storefront
            "tiền thưởng" -> Icons.Default.CardGiftcard
            "giáo dục", "học tập" -> Icons.Default.School
            "sức khỏe" -> Icons.Default.MedicalServices
            "giải trí" -> Icons.Default.Movie
            "tiền điện" -> Icons.Default.ElectricBolt
            "tiền nước" -> Icons.Default.WaterDrop
            "internet" -> Icons.Default.Wifi
            "thuê nhà" -> Icons.Default.Home
            "khác" -> Icons.Default.MoreHoriz
            else -> Icons.Default.Category
        }
    }
}
