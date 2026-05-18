package com.dacs3.smartmoney.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.ui.theme.*
import com.dacs3.smartmoney.util.AppUtils
import com.dacs3.smartmoney.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    transactionId: String? = null,
    onBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var selectedCategory by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEditMode = transactionId != null
    
    // Animation chuyển màu giữa Chi tiêu và Thu nhập
    val themeColor by animateColorAsState(
        targetValue = if (type == "EXPENSE") PinkPrimary else MintPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "themeColor"
    )
    
    val bgColor by animateColorAsState(
        targetValue = if (type == "EXPENSE") PinkLight.copy(alpha = 0.3f) else MintLight.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 500),
        label = "bgColor"
    )

    // Load dữ liệu cũ nếu ở chế độ Edit
    LaunchedEffect(transactionId) {
        if (isEditMode) {
            viewModel.allTransactions.value.find { it.transactionId == transactionId }?.let {
                amount = it.amount.toInt().toString()
                note = it.note
                type = it.type
                selectedCategory = it.categoryName
                date = it.date
            }
        }
    }

    val expenseCats by viewModel.expenseCategories.collectAsState()
    val incomeCats by viewModel.incomeCategories.collectAsState()
    val categories = if (type == "EXPENSE") expenseCats else incomeCats

    // Reset danh mục khi đổi loại giao dịch
    LaunchedEffect(type) {
        if (!isEditMode || (isEditMode && selectedCategory !in categories)) {
            selectedCategory = categories.firstOrNull() ?: ""
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("CHỌN", color = themeColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("HỦY") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Chỉnh sửa giao dịch" else "Thêm giao dịch mới", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Quay lại",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(bgColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Chọn loại giao dịch (Thu/Chi)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    listOf("EXPENSE" to "Chi tiêu", "INCOME" to "Thu nhập").forEach { (t, label) ->
                        val isSelected = type == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) themeColor else Color.Transparent)
                                .clickable { type = t },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Nhập số tiền
                Text(
                    "SỐ TIỀN GIAO DỊCH", 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = themeColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                
                TextField(
                    value = amount,
                    onValueChange = { 
                        if (it.length <= 12 && it.all { char -> char.isDigit() }) {
                            amount = it 
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = themeColor
                    ),
                    placeholder = { 
                        Text(
                            "0 ₫", 
                            modifier = Modifier.fillMaxWidth(), 
                            textAlign = TextAlign.Center, 
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        ) 
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    visualTransformation = { text ->
                        if (text.isEmpty()) {
                            TransformedText(text, OffsetMapping.Identity)
                        } else {
                            val originalText = text.text
                            val number = originalText.toLongOrNull() ?: 0L
                            val formattedText = AppUtils.formatNumber(number) + " ₫"
                            
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
                                    val safeOffset = offset.coerceAtMost(formattedText.length - 2).coerceAtLeast(0)
                                    val formattedBefore = formattedText.substring(0, safeOffset)
                                    return formattedBefore.count { it.isDigit() }
                                }
                            }
                            TransformedText(AnnotatedString(formattedText), offsetMapping)
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form chi tiết
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Chọn ngày
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(themeColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarToday, null, tint = themeColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Ngày thực hiện", fontSize = 12.sp, color = Color.Gray)
                                Text(AppUtils.formatDate(date), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftGray)

                        // Ghi chú
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(themeColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextField(
                                value = note,
                                onValueChange = { newValue -> note = newValue },
                                placeholder = { Text("Thêm ghi chú...", color = Color.LightGray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Chọn danh mục
                Text(
                    "CHỌN DANH MỤC", 
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategory = category }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (isSelected) themeColor else Color.White,
                                        CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = Color.LightGray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    AppUtils.getCategoryIcon(category),
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                category,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) themeColor else Color.Gray,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Nút Lưu
                Button(
                    onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val transaction = Transaction(
                                transactionId = transactionId ?: "",
                                amount = amt,
                                categoryName = selectedCategory,
                                note = note,
                                type = type,
                                date = date
                            )
                            
                            if (isEditMode) {
                                viewModel.updateTransaction(transaction) { success -> if (success) onBack() }
                            } else {
                                viewModel.addNewTransaction(transaction) { success -> if (success) onBack() }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (isEditMode) "CẬP NHẬT" else "LƯU GIAO DỊCH", 
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
