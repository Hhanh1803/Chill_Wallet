package com.dacs3.smartmoney.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkLight,
    onPrimaryContainer = PinkDark,
    
    secondary = MintPrimary,
    onSecondary = Color.White,
    secondaryContainer = MintLight,
    onSecondaryContainer = MintDark,
    
    background = Color.White,
    surface = SoftGray,
    onBackground = DarkText,
    onSurface = DarkText
)

// Vì giao diện cần sự nhẹ nhàng (Pastel), chúng ta sẽ giữ tông sáng chủ đạo
private val DarkColorScheme = lightColorScheme(
    primary = PinkDark,
    onPrimary = Color.White,
    secondary = MintDark,
    onSecondary = Color.White,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2D2D2D)
)

@Composable
fun ChillWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Vô hiệu hóa dynamicColor để ép buộc sử dụng tông Hồng & Xanh Mint [cite: 58]
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
