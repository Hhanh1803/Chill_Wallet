package com.dacs3.smartmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dacs3.smartmoney.ui.navigation.Screen
import com.dacs3.smartmoney.ui.screens.*
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChillWalletTheme {
                val navController = rememberNavController()
                val transactionViewModel: TransactionViewModel = viewModel()

                val startDestination = Screen.Login.route

                NavHost(navController = navController, startDestination = startDestination) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = {
                                transactionViewModel.reloadAllData()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        "Chill Wallet",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = PinkDark
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    DrawerMenuItem(
                        label = "Lịch sử",
                        icon = Icons.Default.History,
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    DrawerMenuItem(
                        label = "Thống kê",
                        icon = Icons.Default.PieChart,
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Stats.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Stats.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    DrawerMenuItem(
                        label = "Ngân sách",
                        icon = Icons.Default.AccountBalanceWallet,
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Budget.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.Budget.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    DrawerMenuItem(
                        label = "Danh mục",
                        icon = Icons.Default.Category,
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.CategoryManagement.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.CategoryManagement.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerMenuItem(
                        label = "Đăng xuất",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        selected = false,
                        color = Color.Red,
                        onClick = {
                            scope.launch { drawerState.close() }
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
        content { scope.launch { drawerState.open() } }
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
