package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import com.dacs3.smartmoney.util.AppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TransactionViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToStats: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val expenseCats by viewModel.expenseCategories.collectAsState()
    val incomeCats by viewModel.incomeCategories.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    val categories = remember(expenseCats, incomeCats) { 
        listOf("Tất cả") + (expenseCats + incomeCats).distinct() 
    }

    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            if (isSearchExpanded) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Tìm kiếm...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchExpanded = false
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng tìm kiếm")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        Text(
                            "Chill Wallet",
                            fontWeight = FontWeight.ExtraBold, 
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = MaterialTheme.colorScheme.primary)
                        }
                        
                        Box {
                            IconButton(onClick = { isFilterMenuExpanded = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Lọc danh mục", tint = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(
                                expanded = isFilterMenuExpanded,
                                onDismissRequest = { isFilterMenuExpanded = false }
                            ) {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            viewModel.onCategoryChange(category)
                                            isFilterMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            if (category != "Tất cả") {
                                                Icon(
                                                    imageVector = AppUtils.getCategoryIcon(category),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Icon(Icons.Default.List, null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (transactionToDelete != null) {
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc muốn xóa khoản chi tiêu này không?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!.transactionId)
                        transactionToDelete = null
                    }) {
                        Text("XÓA", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToDelete = null }) {
                        Text("HỦY")
                    }
                }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategoryChange(category) },
                                label = { Text(category, fontSize = 13.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.LightGray.copy(alpha = 0.5f),
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                item {
                    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                        )
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    "Số dư hiện tại",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    AppUtils.formatCurrency(totalIncome - totalExpense),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    SummaryMiniCard(
                                        label = "Thu nhập",
                                        amount = totalIncome,
                                        color = MintDark,
                                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    SummaryMiniCard(
                                        label = "Chi tiêu",
                                        amount = totalExpense,
                                        color = PinkDark,
                                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
                    val totalExp = expenseTransactions.sumOf { it.amount }
                    
                    if (expenseTransactions.isNotEmpty() && totalExp > 0) {
                        val spendingByCategory = expenseTransactions
                            .groupBy { it.categoryName }
                            .mapValues { it.value.sumOf { t -> t.amount } }
                            .toList()
                            .sortedByDescending { it.second }
                        
                        CategoryPercentageChart(spendingByCategory, totalExp)
                    }
                }

                item {
                    Text(
                        "Lịch sử giao dịch",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.List, 
                                    null, 
                                    modifier = Modifier.size(64.dp), 
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chưa có giao dịch nào.", color = Color.Gray)
                            }
                        }
                    }
                } else {
                    items(transactions) { item ->
                        TransactionItem(
                            transaction = item,
                            onClick = { onNavigateToEdit(item.transactionId) },
                            onLongClickAction = { transactionToDelete = item }
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    label: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    AppUtils.formatCurrency(amount), 
                    color = color, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onLongClickAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClickAction
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconColor = if (transaction.type == "EXPENSE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = AppUtils.getCategoryIcon(transaction.categoryName),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.categoryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (transaction.note.isNotEmpty()) {
                    Text(
                        transaction.note,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
                Text(
                    AppUtils.formatDate(transaction.date),
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            Text(
                text = (if (transaction.type == "EXPENSE") "-" else "+") + AppUtils.formatCurrency(transaction.amount),
                color = if (transaction.type == "EXPENSE") PinkDark else MintDark,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CategoryPercentageChart(
    spendingByCategory: List<Pair<String, Double>>,
    totalExpense: Double
) {
    if (totalExpense <= 0) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Phân bổ chi tiêu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Thanh biểu đồ phần trăm (Stacked bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.2f))
            ) {
                spendingByCategory.forEachIndexed { index, pair ->
                    val weight = (pair.second / totalExpense).toFloat()
                    if (weight > 0.005f) { // Chỉ hiện nếu > 0.5%
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(weight)
                                .background(getCategoryColor(index))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chú thích (Legend)
            val displayCategories = spendingByCategory.take(6)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayCategories.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { pair ->
                            val index = spendingByCategory.indexOf(pair)
                            val percentage = (pair.second / totalExpense * 100).toInt()
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(getCategoryColor(index))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    pair.first,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "$percentage%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF03A9F4), // Light Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFF9C27B0), // Purple
        Color(0xFF009688), // Teal
        Color(0xFF607D8B)  // Blue Grey
    )
    return colors[index % colors.size]
}
