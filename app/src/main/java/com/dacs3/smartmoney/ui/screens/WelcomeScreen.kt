package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.ui.theme.PinkLight
import com.dacs3.smartmoney.ui.theme.PinkPrimary

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .drawBehind {
                // Hiệu ứng "loang màu" (Mesh Gradient) bằng các khối màu siêu mờ
                // Blob hồng nhạt ở góc trên trái
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFE3E8).copy(alpha = 0.7f), Color.Transparent),
                        center = center.copy(x = size.width * 0.1f, y = size.height * 0.1f),
                        radius = size.width * 1.2f
                    ),
                    center = center.copy(x = size.width * 0.1f, y = size.height * 0.1f),
                    radius = size.width * 1.2f
                )
                // Blob xanh mint nhạt ở giữa phải
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE0F2F1).copy(alpha = 0.5f), Color.Transparent),
                        center = center.copy(x = size.width * 0.9f, y = size.height * 0.4f),
                        radius = size.width * 1.0f
                    ),
                    center = center.copy(x = size.width * 0.9f, y = size.height * 0.4f),
                    radius = size.width * 1.0f
                )
                // Blob vàng nhạt ở góc dưới trái
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFDE7).copy(alpha = 0.6f), Color.Transparent),
                        center = center.copy(x = size.width * 0.2f, y = size.height * 0.8f),
                        radius = size.width * 1.5f
                    ),
                    center = center.copy(x = size.width * 0.2f, y = size.height * 0.8f),
                    radius = size.width * 1.5f
                )
            }
    ) {
        // Many subtle background icons to make it "xôm"
        BackgroundOrnaments()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp)) // Giảm spacer đỉnh để nội dung thanh thoát hơn

            // TOP LOGO: Sprout on Wallet (Căn giữa và đối xứng hoàn hảo)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.size(72.dp), // Tăng nhẹ size logo
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 6.dp // Tăng shadow cho logo nổi bật
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PinkPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp).offset(y = 2.dp)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = null,
                            tint = Color(0xFF66BB6A),
                            modifier = Modifier.size(24.dp).offset(x = (-10).dp, y = (-12).dp)
                        )
                    }
                }
                // Sparkles (Phân bổ đối xứng hai bên để cân bằng)
                Icon(
                    Icons.Rounded.AutoAwesome, null,
                    tint = Color(0xFFFFD700).copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp).offset(x = 45.dp, y = (-20).dp)
                )
                Icon(
                    Icons.Rounded.AutoAwesome, null,
                    tint = Color(0xFFFFD700).copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp).offset(x = (-45).dp, y = 15.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Chill Wallet",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = PinkPrimary,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Chi tiêu chill – Tài chính vững vàng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PinkPrimary.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Quản lý chi tiêu thông minh – Sống thoải mái mỗi ngày",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Security Badge
            Surface(
                color = Color(0xFFF0FFF4),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Verified, null, tint = Color(0xFF38A169), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "An toàn bảo mật 24/7",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF38A169),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MAIN ILLUSTRATION: Perfect Symmetrical Ring Layout
            Box(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft glow background
                Surface(
                    modifier = Modifier.size(190.dp),
                    shape = CircleShape,
                    color = PinkPrimary.copy(alpha = 0.05f)
                ) {}

                // Large Wallet Card (Central focus)
                Surface(
                    modifier = Modifier.size(150.dp, 110.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = PinkPrimary,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Savings, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(55.dp))
                        Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(26.dp).offset(x = 22.dp, y = (-18).dp))
                    }
                }

                // Ring of Floating Icons (Mathematically Symmetrical)
                // N, S, E, W (115dp radius)
                FloatingIcon(Icons.Rounded.AutoAwesome, Color(0xFFFFD700), x = 0.dp, y = (-115).dp, size = 32.dp)
                FloatingIcon(Icons.Rounded.Paid, Color(0xFFFBC02D), x = 0.dp, y = 115.dp, size = 32.dp)
                FloatingIcon(Icons.Rounded.Favorite, Color.Red, x = (-115).dp, y = 0.dp, size = 32.dp)
                FloatingIcon(Icons.Rounded.WbCloudy, Color(0xFF90CAF9), x = 115.dp, y = 0.dp, size = 32.dp)
                
                // Diagonals (85dp, 85dp roughly same radius)
                FloatingIcon(Icons.Rounded.Spa, Color(0xFF81C784), x = (-85.dp), y = (-85).dp, size = 28.dp, alpha = 0.6f)
                FloatingIcon(Icons.Rounded.Cake, Color(0xFFFFCC80), x = 85.dp, y = (-85).dp, size = 28.dp, alpha = 0.6f)
                FloatingIcon(Icons.Rounded.MusicNote, Color(0xFFCE93D8), x = (-85.dp), y = 85.dp, size = 28.dp, alpha = 0.6f)
                FloatingIcon(Icons.Rounded.Redeem, Color(0xFFFF8A80), x = 85.dp, y = 85.dp, size = 28.dp, alpha = 0.6f)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FEATURES ROW
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp), // Bo góc lớn hơn cho đúng chất "Chill"
                color = Color.White.copy(alpha = 0.85f), // Giảm độ trong suốt để text rõ hơn
                shadowElevation = 4.dp, // Thêm đổ bóng nhẹ để tạo khối
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 18.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureItem(Icons.Rounded.BarChart, "Theo dõi")
                    FeatureItem(Icons.Rounded.AccountBalance, "Quản lý")
                    FeatureItem(Icons.Rounded.NotificationsActive, "Nhắc nhở")
                    FeatureItem(Icons.Rounded.Security, "Bảo mật")
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // PRIMARY ACTIONS
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ĐĂNG NHẬP", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38A169).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ĐĂNG KÝ NGAY", color = Color(0xFF38A169).copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản? ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    "Đăng ký",
                    color = PinkPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun FloatingIcon(
    icon: ImageVector, 
    color: Color, 
    x: androidx.compose.ui.unit.Dp, 
    y: androidx.compose.ui.unit.Dp, 
    size: androidx.compose.ui.unit.Dp, 
    alpha: Float = 0.7f
) {
    Surface(
        modifier = Modifier.offset(x = x, y = y).size(size),
        shape = CircleShape, color = Color.White, shadowElevation = 3.dp
    ) {
        Icon(icon, null, tint = color.copy(alpha = alpha), modifier = Modifier.padding(size/4))
    }
}

@Composable
fun BackgroundOrnaments() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Rất nhiều icon ẩn hiện ở nền để trông "xôm"
        Icon(Icons.Rounded.BubbleChart, null, tint = PinkPrimary.copy(alpha = 0.04f), modifier = Modifier.size(120.dp).offset(x = (-30).dp, y = 100.dp))
        Icon(Icons.Rounded.WbCloudy, null, tint = Color(0xFF90CAF9).copy(alpha = 0.03f), modifier = Modifier.size(100.dp).offset(x = 280.dp, y = 50.dp))
        Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFD700).copy(alpha = 0.05f), modifier = Modifier.size(40.dp).offset(x = 50.dp, y = 400.dp))
        Icon(Icons.Rounded.AutoAwesome, null, tint = PinkPrimary.copy(alpha = 0.03f), modifier = Modifier.size(80.dp).offset(x = 250.dp, y = 350.dp))
        Icon(Icons.Rounded.MusicNote, null, tint = Color(0xFFCE93D8).copy(alpha = 0.03f), modifier = Modifier.size(60.dp).offset(x = 40.dp, y = 650.dp).rotate(15f))
        Icon(Icons.Rounded.Favorite, null, tint = PinkPrimary.copy(alpha = 0.02f), modifier = Modifier.size(150.dp).offset(x = 200.dp, y = 550.dp).rotate(-20f))
        Icon(Icons.Rounded.Eco, null, tint = Color(0xFF81C784).copy(alpha = 0.03f), modifier = Modifier.size(140.dp).offset(x = (-40).dp, y = 450.dp).rotate(-15f))
        Icon(Icons.Rounded.Paid, null, tint = Color(0xFFFBC02D).copy(alpha = 0.02f), modifier = Modifier.size(100.dp).offset(x = 300.dp, y = 700.dp))
        
        // Thêm các icon mới rải rác
        Icon(Icons.Rounded.Pets, null, tint = Color(0xFF8D6E63).copy(alpha = 0.02f), modifier = Modifier.size(50.dp).offset(x = 20.dp, y = 50.dp))
        Icon(Icons.Rounded.Celebration, null, tint = Color(0xFFFF7043).copy(alpha = 0.02f), modifier = Modifier.size(70.dp).offset(x = 320.dp, y = 200.dp))
        Icon(Icons.Rounded.Coffee, null, tint = Color(0xFF6D4C41).copy(alpha = 0.03f), modifier = Modifier.size(45.dp).offset(x = 280.dp, y = 600.dp))
        Icon(Icons.Rounded.Lightbulb, null, tint = Color(0xFFFFEB3B).copy(alpha = 0.03f), modifier = Modifier.size(40.dp).offset(x = 40.dp, y = 300.dp))
        Icon(Icons.Rounded.SelfImprovement, null, tint = Color(0xFF26A69A).copy(alpha = 0.02f), modifier = Modifier.size(100.dp).offset(x = 100.dp, y = 750.dp))
        
        // Thêm lớp icon "xôm" hơn nữa
        Icon(Icons.Rounded.AirplanemodeActive, null, tint = Color(0xFF81D4FA).copy(alpha = 0.02f), modifier = Modifier.size(60.dp).offset(x = 200.dp, y = 120.dp).rotate(-30f))
        Icon(Icons.Rounded.ShoppingCart, null, tint = Color(0xFFB0BEC5).copy(alpha = 0.02f), modifier = Modifier.size(50.dp).offset(x = 10.dp, y = 200.dp))
        Icon(Icons.Rounded.Restaurant, null, tint = Color(0xFFFFAB91).copy(alpha = 0.02f), modifier = Modifier.size(45.dp).offset(x = 320.dp, y = 600.dp))
        Icon(Icons.Rounded.Icecream, null, tint = Color(0xFFF48FB1).copy(alpha = 0.02f), modifier = Modifier.size(40.dp).offset(x = 60.dp, y = 550.dp))
        Icon(Icons.Rounded.DirectionsBike, null, tint = Color(0xFFA5D6A7).copy(alpha = 0.02f), modifier = Modifier.size(55.dp).offset(x = 240.dp, y = 800.dp))
        Icon(Icons.Rounded.BakeryDining, null, tint = Color(0xFFFFCC80).copy(alpha = 0.02f), modifier = Modifier.size(50.dp).offset(x = 300.dp, y = 380.dp))
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = CircleShape,
            color = PinkPrimary.copy(alpha = 0.08f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    null, 
                    tint = PinkPrimary, 
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold, 
            color = Color(0xFF555555), 
            textAlign = TextAlign.Center
        )
    }
}
