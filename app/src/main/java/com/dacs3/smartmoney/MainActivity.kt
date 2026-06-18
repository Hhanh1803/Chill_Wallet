package com.dacs3.smartmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.dacs3.smartmoney.data.PreferenceManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.dacs3.smartmoney.ui.navigation.Screen
import com.dacs3.smartmoney.ui.screens.*
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.dacs3.smartmoney.ui.screens.AdminScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val preferenceManager = remember { PreferenceManager(applicationContext) }
            val isDarkMode by preferenceManager.isDarkMode.collectAsState(initial = false)

            ChillWalletTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                val transactionViewModel: TransactionViewModel = viewModel()

                val startDestination = Screen.Welcome.route

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Welcome.route) {
                        WelcomeScreen(
                            onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                        )
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                    return@LoginScreen
                                }
                                
                                val db = FirebaseFirestore.getInstance()
                                val uid = user.uid
                                
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            val role = document.getString("role") ?: "USER"
                                            android.util.Log.d("Login", "User role: $role")
                                            transactionViewModel.reloadAllData()
                                            
                                            if (role == "ADMIN") {
                                                navController.navigate(Screen.Admin.route) {
                                                    popUpTo(Screen.Login.route) { inclusive = true }
                                                }
                                            } else {
                                                navController.navigate(Screen.Home.route) {
                                                    popUpTo(Screen.Login.route) { inclusive = true }
                                                }
                                            }
                                        } else {
                                            // 2. Nếu CHƯA có dữ liệu, tạo bản ghi mặc định là USER
                                            val userData = hashMapOf(
                                                "uid" to uid,
                                                "displayName" to (user.displayName ?: "Người dùng mới"),
                                                "fullName" to (user.displayName ?: "Người dùng mới"),
                                                "email" to (user.email ?: ""),
                                                "role" to "USER", // Mặc định là USER
                                                "isLocked" to false,
                                                "joinDate" to System.currentTimeMillis()
                                            )
                                            db.collection("users").document(uid)
                                                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                                .addOnSuccessListener {
                                                    transactionViewModel.reloadAllData()
                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        android.util.Log.e("Login", "Error: ${e.message}")
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                            },
                            onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                        )
                    }
                    composable(Screen.Register.route) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                transactionViewModel.reloadAllData()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            onBackToLogin = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Home.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            HomeScreen(
                                viewModel = transactionViewModel,
                                onNavigateToAdd = { navController.navigate(Screen.Add.route) },
                                onNavigateToEdit = { id -> navController.navigate("edit/$id") },
                                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.Stats.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            StatsScreen(
                                viewModel = transactionViewModel,
                                onBack = { navController.popBackStack() },
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.Add.route) {
                        AddTransactionScreen(
                            viewModel = transactionViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Edit.route) { backStackEntry ->
                        val transactionId = backStackEntry.arguments?.getString("transactionId")
                        AddTransactionScreen(
                            viewModel = transactionViewModel,
                            transactionId = transactionId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Budget.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            BudgetScreen(
                                viewModel = transactionViewModel,
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.CategoryManagement.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            CategoryManagementScreen(
                                viewModel = transactionViewModel,
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.Settings.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            SettingsScreen(
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.Profile.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            ProfileScreen(
                                onOpenDrawer = onOpenDrawer
                            )
                        }
                    }
                    composable(Screen.Admin.route) {
                        val user = FirebaseAuth.getInstance().currentUser
                        var isAdmin by remember { mutableStateOf<Boolean?>(null) }
                        
                        LaunchedEffect(user?.uid) {
                            if (user == null) {
                                isAdmin = false
                                return@LaunchedEffect
                            }
                            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                                .addOnSuccessListener { doc ->
                                    isAdmin = doc.getString("role") == "ADMIN"
                                }
                                .addOnFailureListener {
                                    isAdmin = false
                                }
                        }

                        when (isAdmin) {
                            true -> AdminScreen(onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            })
                            false -> {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Admin.route) { inclusive = true }
                                    }
                                }
                            }
                            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PinkPrimary)
                            }
                        }
                    }
                    composable(Screen.GroupFund.route) {
                        MainScaffold(navController) { onOpenDrawer ->
                            val scanResult = navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.get<String>("scan_result")
                            
                            GroupFundScreen(
                                onNavigateToDetail = { groupId ->
                                    navController.navigate("group_detail/$groupId")
                                },
                                onNavigateToScanner = {
                                    navController.navigate(Screen.QRScanner.route)
                                },
                                onOpenDrawer = onOpenDrawer,
                                scanResult = scanResult
                            )
                            
                            // Clear result after use
                            SideEffect {
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.remove<String>("scan_result")
                            }
                        }
                    }
                    composable(Screen.GroupDetail.route) { backStackEntry ->
                        val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                        GroupDetailScreen(
                            groupId = groupId,
                            onBack = { navController.popBackStack() },
                            onNavigateToAddTransaction = { id ->
                                navController.navigate("add_group_transaction/$id")
                            }
                        )
                    }
                    composable(Screen.AddGroupTransaction.route) { backStackEntry ->
                        val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                        AddGroupTransactionScreen(
                            groupId = groupId,
                            viewModel = transactionViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.QRScanner.route) {
                        QRScannerScreen(
                            onScanSuccess = { code ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("scan_result", code)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScaffold(
    navController: androidx.navigation.NavHostController,
    content: @Composable (onOpenDrawer: () -> Unit) -> Unit
) {
    val transactionViewModel: TransactionViewModel = viewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Stats,
        Screen.Add,
        Screen.GroupFund,
        Screen.Profile
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                ) {
                    val user = FirebaseAuth.getInstance().currentUser
                    var userRole by remember { mutableStateOf("USER") }

                    LaunchedEffect(user?.uid) {
                        user?.uid?.let { uid ->
                            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                                .addOnSuccessListener { doc ->
                                    userRole = doc.getString("role") ?: "USER"
                                }
                                .addOnFailureListener {
                                    userRole = "USER"
                                }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // User Profile Header in Drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = PinkPrimary.copy(alpha = 0.1f)
                        ) {
                            AsyncImage(
                                model = user?.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = rememberVectorPainter(Icons.Default.Person),
                                error = rememberVectorPainter(Icons.Default.Person)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = user?.displayName ?: "Người dùng",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = user?.email ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        thickness = 1.dp,
                        color = SoftGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    listOf(
                        Screen.Profile to R.string.menu_profile,
                        Screen.Home to R.string.menu_home,
                        Screen.GroupFund to R.string.group_fund_title,
                        Screen.Stats to R.string.menu_stats,
                        Screen.Budget to R.string.menu_budget,
                        Screen.Settings to R.string.menu_settings
                    ).let { baseList ->
                        if (userRole == "ADMIN") {
                            baseList + (Screen.Admin to R.string.menu_admin)
                        } else baseList
                    }.forEach { (screen, labelRes) ->
                        DrawerMenuItem(
                            label = if (screen == Screen.Admin) "Quản trị" else stringResource(labelRes),
                            icon = screen.icon ?: Icons.Default.Menu,
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerMenuItem(
                        label = stringResource(R.string.logout),
                        icon = Icons.AutoMirrored.Filled.Logout,
                        selected = false,
                        color = Color.Red,
                        onClick = {
                            scope.launch { drawerState.close() }
                            transactionViewModel.clearData()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        if (screen == Screen.Add) {
                            // Nút Thêm ở chính giữa với giao diện đặc biệt
                            NavigationBarItem(
                                icon = {
                                    Surface(
                                        shape = CircleShape,
                                        color = PinkPrimary,
                                        modifier = Modifier.size(48.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = stringResource(R.string.menu_add),
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                },
                                label = { Text(stringResource(R.string.menu_add), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PinkPrimary) },
                                selected = false,
                                onClick = { navController.navigate(Screen.Add.route) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                )
                            )
                        } else {
                            val labelRes = when(screen) {
                                Screen.Home -> R.string.menu_home
                                Screen.Stats -> R.string.menu_stats
                                Screen.GroupFund -> R.string.group_fund_title
                                Screen.Profile -> R.string.menu_profile
                                else -> R.string.app_name
                            }
                            NavigationBarItem(
                                icon = { 
                                    screen.icon?.let { 
                                        Icon(
                                            imageVector = it, 
                                            contentDescription = stringResource(labelRes),
                                            modifier = Modifier.size(24.dp)
                                        ) 
                                    } 
                                },
                                label = { 
                                    Text(
                                        stringResource(labelRes),
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PinkPrimary,
                                    selectedTextColor = PinkPrimary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = PinkLight.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content { scope.launch { drawerState.open() } }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    color: Color = DarkText
) {
    val backgroundColor = if (selected) PinkLight else Color.Transparent
    val contentColor = if (selected) PinkDark else color

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
