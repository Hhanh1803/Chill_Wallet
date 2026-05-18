package com.dacs3.smartmoney.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dacs3.smartmoney.ui.screens.CategoryPercentageChart
import com.dacs3.smartmoney.ui.theme.ChillWalletTheme

@Preview(showBackground = true)
@Composable
fun CategoryPercentageChartPreview() {
    ChillWalletTheme {
        CategoryPercentageChart(
            spendingByCategory = listOf(
                "Ăn uống" to 500000.0,
                "Di chuyển" to 200000.0,
                "Mua sắm" to 150000.0,
                "Giải trí" to 100000.0,
                "Khác" to 50000.0
            ),
            totalExpense = 1000000.0
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryPercentageChartDarkPreview() {
    ChillWalletTheme {
        CategoryPercentageChart(
            spendingByCategory = listOf(
                "Ăn uống" to 500000.0,
                "Di chuyển" to 200000.0,
                "Mua sắm" to 150000.0,
                "Giải trí" to 100000.0,
                "Khác" to 50000.0
            ),
            totalExpense = 1000000.0
        )
    }
}
