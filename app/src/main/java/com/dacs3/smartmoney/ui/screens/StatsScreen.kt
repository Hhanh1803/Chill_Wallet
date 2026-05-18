package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import com.dacs3.smartmoney.util.AppUtils
import com.dacs3.smartmoney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val selectedTab by viewModel.selectedStatsTab.collectAsState()
    val selectedType by viewModel.selectedStatsType.collectAsState()
    
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStatsList.collectAsState()
    val yearlyStats by viewModel.yearlyStatsList.collectAsState()
    val statsComparison by viewModel.statsComparison.collectAsState()

    val currentStats = when (selectedTab) {
        0 -> weeklyStats
        1 -> monthlyStats
        else -> yearlyStats
    }

    val totalIncome = currentStats.sumOf { it.second }
    val totalExpense = currentStats.sumOf { it.third }
    val diff = totalIncome - totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Biến động thu chi", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Info, contentDescription = "Support")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFE4E1).copy(alpha = 0.5f)
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Tab Selector: Theo tuần, Theo tháng, Theo năm
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf("Theo tuần", "Theo tháng", "Theo năm")
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { viewModel.onStatsTabChange(index) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) PinkDark else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-tabs: Thu nhập, Chi tiêu, Chênh lệch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val types = listOf("Thu nhập", "Chi tiêu", "Chênh lệch")
                        types.forEachIndexed { index, label ->
                            val isSelected = selectedType == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onStatsTypeChange(index) }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) PinkDark else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .height(3.dp)
                                            .width(40.dp)
                                            .background(PinkDark, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    )
                                }
                            }
                        }
                    }

                    // Content based on sub-tab
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val label = when(selectedTab) {
                            0 -> "tuần này"
                            1 -> "tháng này"
                            else -> "năm nay"
                        }
                        
                        Text(
                            "Tổng ${listOf("thu nhập", "chi tiêu", "chênh lệch")[selectedType]} $label",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        
                        val amount = when(selectedType) {
                            0 -> totalIncome
                            1 -> totalExpense
                            else -> diff
                        }
                        
                        Text(
                            AppUtils.formatCurrency(amount),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selectedType == 2 && diff < 0) Color.Red else Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Percentage comparison
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (statsComparison.second) MintPrimary.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (statsComparison.second) Icons.Default.Info else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (statsComparison.second) MintDark else Color.Red,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${if (statsComparison.second) "Tăng" else "Giảm"} ${AppUtils.formatCurrency(statsComparison.first)} so với kỳ trước",
                                color = if (statsComparison.second) MintDark else Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "Biến động",
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Custom Bar Chart
                        MomoStyleBarChart(
                            data = currentStats,
                            type = selectedType,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction List Style Stats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                currentStats.reversed().forEach { stat ->
                    StatRowItem(stat, selectedTab)
                    HorizontalDivider(color = Color(0xFFF2F2F2), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun MomoStyleBarChart(
    data: List<Triple<String, Double, Double>>,
    type: Int,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Không có dữ liệu", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selectedIndex by remember(data, type) { mutableIntStateOf(-1) }

    val displayValues = data.map {
        when(type) {
            0 -> it.second
            1 -> it.third
            else -> it.second - it.third
        }
    }

    val maxValue = displayValues.maxOf { it }.coerceAtLeast(0.0)
    val minValue = displayValues.minOf { it }.coerceAtMost(0.0)
    
    val range = (maxValue - minValue).coerceAtLeast(1.0)
    val zeroRatio = if (range == 0.0) 1f else (maxValue / range).toFloat().coerceIn(0f, 1f)

    val spacing = if (data.size > 10) 2.dp else 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(data, type) {
                detectTapGestures { offset ->
                    val width = size.width
                    val itemWidth = width / data.size
                    val index = (offset.x / itemWidth).toInt().coerceIn(0, data.size - 1)
                    selectedIndex = if (selectedIndex == index) -1 else index
                }
            }
    ) {
        // Selection indicator / Tooltip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedIndex != -1) {
                val value = displayValues[selectedIndex]
                val label = data[selectedIndex].first
                Surface(
                    color = PinkDark,
                    shape = RoundedCornerShape(4.dp),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        "$label: ${AppUtils.formatCurrency(value)}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Background Grid lines
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                repeat(5) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                }
            }

            // Zero line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(zeroRatio)
                    .drawWithContent {
                        drawContent()
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.4f),
                            start = androidx.compose.ui.geometry.Offset(0f, size.height),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                displayValues.forEachIndexed { index, value ->
                    val isPositive = value >= 0
                    val isSelected = selectedIndex == index
                    
                    val animatedRatio by animateFloatAsState(
                        targetValue = (Math.abs(value) / range).toFloat().coerceIn(0.01f, 1f),
                        animationSpec = tween(500),
                        label = "barHeight"
                    )

                    val barColor = when (type) {
                        0 -> if (isSelected) MintDark else Color(0xFF00C853)
                        1 -> if (isSelected) PinkDark else Color(0xFFFF3366)
                        else -> if (isPositive) {
                            if (isSelected) Color(0xFF1976D2) else Color(0xFF2196F3)
                        } else {
                            if (isSelected) Color(0xFFE64A19) else Color(0xFFFF7043)
                        }
                    }

                    val gradient = Brush.verticalGradient(
                        colors = listOf(barColor.copy(alpha = 0.8f), barColor)
                    )

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        // Positive Space
                        Box(
                            modifier = Modifier
                                .weight(zeroRatio.coerceAtLeast(0.01f))
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (isPositive && value != 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedRatio / zeroRatio)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(gradient)
                                )
                            }
                        }

                        // Negative Space
                        Box(
                            modifier = Modifier
                                .weight((1f - zeroRatio).coerceAtLeast(0.01f))
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (!isPositive && value != 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(animatedRatio / (1f - zeroRatio))
                                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(gradient)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            data.forEachIndexed { index, stat ->
                val isSelected = selectedIndex == index
                val showLabel = data.size <= 12 || index % (data.size / 6).coerceAtLeast(1) == 0 || index == data.size - 1
                Text(
                    if (showLabel) stat.first else "",
                    modifier = Modifier.weight(1f),
                    fontSize = 8.sp,
                    color = if (isSelected) PinkDark else Color.Gray,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatRowItem(stat: Triple<String, Double, Double>, selectedTab: Int) {
    val diff = stat.second - stat.third
    val isPositive = diff >= 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stat.first,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DarkText
                    )
                    val subLabel = when(selectedTab) {
                        0 -> "Tuần"
                        1 -> "Tháng"
                        else -> "Năm"
                    }
                    Text(subLabel, fontSize = 8.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MintPrimary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Thu:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppUtils.formatCurrency(stat.second), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(PinkPrimary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chi:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppUtils.formatCurrency(stat.third), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("Dư", fontSize = 9.sp, color = Color.Gray)
                Text(
                    (if (isPositive && diff > 0) "+" else "") + AppUtils.formatCurrency(diff),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isPositive) Color(0xFF00C853) else Color(0xFFFF3366)
                )
            }
        }
    }
}
