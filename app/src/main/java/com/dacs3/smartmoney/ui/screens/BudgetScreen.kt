package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.res.stringResource
import com.dacs3.smartmoney.R
import com.dacs3.smartmoney.data.model.Budget
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.util.AppUtils
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: TransactionViewModel,
    onOpenDrawer: () -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    val themePrimary = PinkDark

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.budget_title), 
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
                containerColor = themePrimary,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_budget), modifier = Modifier.size(30.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AccountBalanceWallet, 
                            null, 
                            modifier = Modifier.size(80.dp), 
                            tint = Color.LightGray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.no_budgets), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(budgets) { budget ->
                        val spent = transactions.filter { 
                            it.categoryName == budget.categoryName && it.type == "EXPENSE"
                        }.sumOf { it.amount }

                        BudgetCard(budget = budget, spent = spent)
                    }
                }
            }
        }

        if (showAddDialog) {
            val expenseCats by viewModel.expenseCategories.collectAsState()
            AddBudgetDialog(
                expenseCategories = expenseCats,
                onDismiss = { showAddDialog = false },
                onConfirm = { category, amount ->
                    val calendar = Calendar.getInstance()
                    viewModel.saveBudget(
                        Budget(
                            categoryName = category,
                            limitAmount = amount,
                            month = calendar.get(Calendar.MONTH),
                            year = calendar.get(Calendar.YEAR)
                        )
                    ) {
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun BudgetCard(budget: Budget, spent: Double) {
    val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
    val isOverBudget = progress > 1f
    val accentColor = if (isOverBudget) Color(0xFFE57373) else PinkDark

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = AppUtils.getCategoryIcon(budget.categoryName),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        budget.categoryName, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
                Text(
                    AppUtils.formatCurrency(budget.limitAmount), 
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(stringResource(R.string.used), style = MaterialTheme.typography.labelSmall, color = MediumText)
                    Text(
                        AppUtils.formatCurrency(spent), 
                        color = if (isOverBudget) Color(0xFFE57373) else DarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${(progress * 100).toInt()}%", 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    expenseCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var category by remember { mutableStateOf(expenseCategories.firstOrNull() ?: "") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val themePrimary = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.setup_budget), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.menu_categories)) },
                        leadingIcon = { Icon(AppUtils.getCategoryIcon(category), null, tint = themePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themePrimary,
                            focusedLabelColor = themePrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        expenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                },
                                leadingIcon = { 
                                    Icon(
                                        AppUtils.getCategoryIcon(cat), 
                                        null,
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                            amount = it 
                        }
                    },
                    label = { Text(stringResource(R.string.budget_limit)) },
                    placeholder = { Text("0") },
                    prefix = { Text("₫ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    visualTransformation = { text ->
                        if (text.isEmpty()) {
                            TransformedText(text, OffsetMapping.Identity)
                        } else {
                            val originalText = text.text
                            val number = originalText.toLongOrNull() ?: 0L
                            val formattedText = AppUtils.formatNumber(number)
                            
                            val offsetMapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int {
                                    if (offset <= 0) return 0
                                    val safeOffset = offset.coerceAtMost(originalText.length)
                                    val digitsBefore = originalText.substring(0, safeOffset)
                                    if (digitsBefore.isEmpty()) return 0
                                    val formattedBefore = AppUtils.formatNumber(digitsBefore.toLongOrNull() ?: 0L)
                                    return formattedBefore.length
                                }

                                override fun transformedToOriginal(offset: Int): Int {
                                    if (offset <= 0) return 0
                                    val safeOffset = offset.coerceAtMost(formattedText.length)
                                    val formattedBefore = formattedText.substring(0, safeOffset)
                                    return formattedBefore.count { it.isDigit() }
                                }
                            }
                            TransformedText(AnnotatedString(formattedText), offsetMapping)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themePrimary,
                        focusedLabelColor = themePrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(category, amount.toDoubleOrNull() ?: 0.0) },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = themePrimary)
            ) {
                Text(stringResource(R.string.setup))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}