package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.data.model.Category
import com.dacs3.smartmoney.util.AppUtils
import com.dacs3.smartmoney.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: TransactionViewModel,
    onOpenDrawer: () -> Unit
) {
    val customCategories by viewModel.categories.collectAsState()
    val expenseCats by viewModel.expenseCategories.collectAsState()
    val incomeCats by viewModel.incomeCategories.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("EXPENSE") }

    val currentDisplayNames = if (selectedType == "EXPENSE") expenseCats else incomeCats
    val defaultNames = if (selectedType == "EXPENSE") AppUtils.EXPENSE_CATEGORIES else AppUtils.INCOME_CATEGORIES

    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val currentThemeColor = if (selectedType == "EXPENSE") themePrimary else themeSecondary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Quản lý danh mục", 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = themePrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = currentThemeColor,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm danh mục", modifier = Modifier.size(30.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = if (selectedType == "EXPENSE") 0 else 1,
                containerColor = Color.Transparent,
                contentColor = currentThemeColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (selectedType == "EXPENSE") 0 else 1]),
                        color = currentThemeColor
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedType == "EXPENSE",
                    onClick = { selectedType = "EXPENSE" },
                    text = { Text("CHI TIÊU", fontWeight = if (selectedType == "EXPENSE") FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = themePrimary,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = selectedType == "INCOME",
                    onClick = { selectedType = "INCOME" },
                    text = { Text("THU NHẬP", fontWeight = if (selectedType == "INCOME") FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = themeSecondary,
                    unselectedContentColor = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(currentDisplayNames) { categoryName ->
                    val customCat = customCategories.find { it.name == categoryName && it.type == selectedType }
                    val isDefault = categoryName in defaultNames
                    
                    CategoryItem(
                        name = categoryName,
                        isDefault = isDefault,
                        themeColor = currentThemeColor,
                        onDelete = {
                            customCat?.let { viewModel.deleteCategory(it.id) }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddCategoryDialog(
                type = selectedType,
                themeColor = currentThemeColor,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, iconName ->
                    viewModel.addCategory(name, selectedType, iconName) {
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryItem(name: String, isDefault: Boolean, themeColor: Color, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = themeColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = AppUtils.getCategoryIcon(name),
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                    if (isDefault) {
                        Text("Mặc định", fontSize = 11.sp, color = Color.LightGray)
                    } else {
                        Text("Tùy chỉnh", fontSize = 11.sp, color = themeColor)
                    }
                }
            }
            if (!isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    type: String,
    themeColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm danh mục mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Loại: ${if (type == "EXPENSE") "Chi tiêu" else "Thu nhập"}",
                    color = themeColor,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên danh mục") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        focusedLabelColor = themeColor
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, "Category") },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("THÊM")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}
