package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.data.model.Category
import com.dacs3.smartmoney.data.model.User
import com.dacs3.smartmoney.data.remote.FirebaseSource
import com.dacs3.smartmoney.ui.theme.*
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Người dùng", "Danh mục", "Nhóm", "Hệ thống")
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "QUẢN TRỊ HỆ THỐNG", 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 18.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onLogout,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab, 
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            ) 
                        }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                when (selectedTab) {
                    0 -> UserManagementTab(snackbarHostState)
                    1 -> CategoryManagementTab(snackbarHostState)
                    2 -> GroupManagementTab(snackbarHostState)
                    3 -> SystemStatsTab()
                }
            }
        }
    }
}

@Composable
fun UserManagementTab(snackbarHostState: SnackbarHostState) {
    val firebaseSource = remember { FirebaseSource() }
    val users by firebaseSource.getAllUsersRealtime().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var userToLock by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { 
            it.getBestName().contains(searchQuery, ignoreCase = true) || 
            it.email.contains(searchQuery, ignoreCase = true) 
        }
    }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Tìm kiếm người dùng...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredUsers) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.getBestName().take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    user.getBestName(), 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (user.role == "ADMIN") {
                                    Surface(
                                        modifier = Modifier.padding(start = 8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ADMIN", 
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Black, 
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Text(
                                user.email, 
                                fontSize = 13.sp, 
                                color = Color.Gray
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { userToLock = user },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (user.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (user.isLocked) Color.Red else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { userToDelete = user },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = "Delete", 
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog xác nhận khóa/mở khóa
    userToLock?.let { user ->
        AlertDialog(
            onDismissRequest = { userToLock = null },
            title = { Text(if (user.isLocked) "Mở khóa tài khoản" else "Khóa tài khoản", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Bạn có chắc chắn muốn ${if (user.isLocked) "mở khóa" else "khóa"} tài khoản của ${user.getBestName()}?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val result = firebaseSource.updateUserLockStatus(user.uid, !user.isLocked)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("Đã cập nhật trạng thái tài khoản")
                        }
                        userToLock = null
                    }
                }) { Text("Xác nhận", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { userToLock = null }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Dialog xác nhận xóa tài khoản
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Xóa tài khoản", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Hành động này không thể hoàn tác. Bạn có chắc chắn muốn xóa tài khoản của ${user.getBestName()}?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = firebaseSource.deleteUserAccount(user.uid)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Đã xóa tài khoản thành công")
                            }
                            userToDelete = null
                        }
                    }
                ) { Text("Xóa vĩnh viễn", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun CategoryManagementTab(snackbarHostState: SnackbarHostState) {
    val firebaseSource = remember { FirebaseSource() }
    val categories by firebaseSource.getGlobalCategoriesRealtime().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("EXPENSE") }

    val displayCategories = remember(categories) {
        val systemCategoryNames = categories.map { it.name.lowercase() }.toSet()
        val defaults = (com.dacs3.smartmoney.util.AppUtils.EXPENSE_CATEGORIES.map { Category(id = "default", name = it, type = "EXPENSE") } +
                       com.dacs3.smartmoney.util.AppUtils.INCOME_CATEGORIES.map { Category(id = "default", name = it, type = "INCOME") })
                       .filter { it.name.lowercase() !in systemCategoryNames }
        (categories + defaults).sortedWith(compareBy({ it.type }, { it.name }))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Nhấn biểu tượng đám mây để đồng bộ danh mục mặc định vào hệ thống quản lý.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(displayCategories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(category.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                            supportingContent = { 
                                Text(
                                    if(category.type == "EXPENSE") "Chi tiêu" else "Thu nhập",
                                    color = if(category.type == "EXPENSE") MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                ) 
                            },
                            trailingContent = {
                                if (category.id != "default") {
                                    Row {
                                        IconButton(onClick = { 
                                            editingCategory = category
                                            categoryName = category.name
                                            categoryType = category.type
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { categoryToDelete = category }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                                        }
                                    }
                                } else {
                                    IconButton(onClick = {
                                        scope.launch {
                                            val result = firebaseSource.addGlobalCategory(category.copy(id = ""))
                                            if (result.isSuccess) {
                                                snackbarHostState.showSnackbar("Đã đồng bộ danh mục vào hệ thống")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = "Add to System", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { 
                showAddDialog = true
                categoryName = ""
                categoryType = "EXPENSE"
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category")
        }
    }

    // Dialog Thêm/Sửa danh mục
    if (showAddDialog || editingCategory != null) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingCategory = null 
            },
            title = { Text(if(showAddDialog) "Thêm danh mục hệ thống" else "Sửa danh mục", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Tên danh mục") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        RadioButton(
                            selected = categoryType == "EXPENSE", 
                            onClick = { categoryType = "EXPENSE" },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Chi tiêu", color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = categoryType == "INCOME", 
                            onClick = { categoryType = "INCOME" },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Thu nhập", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (categoryName.isNotBlank()) {
                        scope.launch {
                            val result = if (showAddDialog) {
                                firebaseSource.addGlobalCategory(Category(name = categoryName, iconName = "Default", type = categoryType))
                            } else {
                                editingCategory?.let {
                                    firebaseSource.updateGlobalCategory(it.copy(name = categoryName, type = categoryType))
                                } ?: Result.failure(Exception())
                            }
                            
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar(if(showAddDialog) "Đã thêm danh mục mới" else "Đã cập nhật danh mục")
                            }
                            
                            categoryName = ""
                            showAddDialog = false
                            editingCategory = null
                        }
                    }
                }) { Text("Lưu", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    editingCategory = null 
                }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Dialog xác nhận xóa danh mục
    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Xóa danh mục", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Bạn có chắc chắn muốn xóa danh mục '${category.name}' khỏi hệ thống?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = firebaseSource.deleteGlobalCategory(category.id)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Đã xóa danh mục")
                            }
                            categoryToDelete = null
                        }
                    }
                ) { Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun GroupManagementTab(snackbarHostState: SnackbarHostState) {
    val firebaseSource = remember { FirebaseSource() }
    val groups by firebaseSource.getAllGroupsRealtime().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var groupToDelete by remember { mutableStateOf<com.dacs3.smartmoney.data.model.GroupFund?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups) { group ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Số dư: ${com.dacs3.smartmoney.util.AppUtils.formatCurrency(group.balance)} • ${group.memberUids.size} thành viên", 
                            fontSize = 13.sp, 
                            color = Color.Gray
                        )
                        Text("Mã mời: ${group.inviteCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }

                    IconButton(onClick = { groupToDelete = group }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = Color.LightGray)
                    }
                }
            }
        }
    }

    groupToDelete?.let { group ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Xóa nhóm quỹ", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Bạn có chắc chắn muốn xóa nhóm '${group.name}'? Mọi dữ liệu giao dịch trong nhóm sẽ bị xóa vĩnh viễn.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = firebaseSource.deleteGroupFund(group.groupId)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("Đã xóa nhóm quỹ")
                            }
                            groupToDelete = null
                        }
                    }
                ) { Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SystemStatsTab() {
    val firebaseSource = remember { FirebaseSource() }
    var stats by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = firebaseSource.getGlobalStats()
        if (result.isSuccess) {
            stats = result.getOrNull() ?: emptyMap()
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Thống kê hệ thống", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            
            StatCard("Tổng số người dùng", stats["totalUsers"]?.toString() ?: "0", Icons.Default.People)
            StatCard("Tổng số nhóm quỹ", stats["totalGroups"]?.toString() ?: "0", Icons.Default.Groups)
            StatCard("Tổng giao dịch", stats["totalTransactions"]?.toString() ?: "0", Icons.Default.ReceiptLong)
            
            val mostUsedCat = stats["mostUsedCategory"]?.toString() ?: "N/A"
            val mostUsedCount = stats["mostUsedCategoryCount"]?.toString() ?: "0"
            StatCard("Danh mục phổ biến", "$mostUsedCat ($mostUsedCount)", Icons.Default.Category)
            
            StatCard("Tổng thu nhập hệ thống", String.format(Locale.getDefault(), "%,.0f đ", stats["totalIncome"] as? Double ?: 0.0), Icons.AutoMirrored.Filled.TrendingUp)
            StatCard("Tổng chi tiêu hệ thống", String.format(Locale.getDefault(), "%,.0f đ", stats["totalExpense"] as? Double ?: 0.0), Icons.AutoMirrored.Filled.TrendingDown)
            
            Spacer(modifier = Modifier.weight(1f))
            Text("Lưu ý: Thống kê này được cập nhật theo thời gian thực từ hệ thống.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
