package com.dacs3.smartmoney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.viewmodel.TransactionViewModel
import com.dacs3.smartmoney.util.AppUtils
import kotlinx.coroutines.launch

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
    var selectedCategory by remember { mutableStateOf("Ăn uống") }
    var expanded by remember { mutableStateOf(false) }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEditMode = transactionId != null
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val currentTypeColor = if (type == "EXPENSE") themePrimary else themeSecondary

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

    val categories = if (type == "EXPENSE") {
        expenseCats
    } else {
        incomeCats
    }

    // Đảm bảo selectedCategory hợp lệ khi đổi type
    LaunchedEffect(type, expenseCats, incomeCats) {
        if (!isEditMode) {
            if (type == "EXPENSE" && (selectedCategory !in expenseCats)) {
                selectedCategory = expenseCats.firstOrNull() ?: ""
            } else if (type == "INCOME" && (selectedCategory !in incomeCats)) {
                selectedCategory = incomeCats.firstOrNull() ?: ""
            }
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    showDatePicker = false
                }) { Text("CHỌN") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("HỦY") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditMode) "Chỉnh sửa" else "Thêm mới", 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Chọn loại giao dịch (Thu/Chi)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SegmentedButton(
                    selected = type == "EXPENSE",
                    onClick = { type = "EXPENSE" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = themePrimary.copy(alpha = 0.2f),
                        activeContentColor = themePrimary,
                        activeBorderColor = themePrimary
                    )
                ) {
                    Text("CHI TIÊU", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = type == "INCOME",
                    onClick = { type = "INCOME" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = themeSecondary.copy(alpha = 0.2f),
                        activeContentColor = themeSecondary,
                        activeBorderColor = themeSecondary
                    )
                ) {
                    Text("THU NHẬP", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Nhập số tiền lớn
            Text(
                "SỐ TIỀN", 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.Gray,
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
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = currentTypeColor
                ),
                placeholder = { 
                    Text(
                        "0", 
                        modifier = Modifier.fillMaxWidth(), 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = currentTypeColor.copy(alpha = 0.3f)
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
            
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(currentTypeColor.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Chọn ngày
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = currentTypeColor)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Ngày giao dịch", fontSize = 12.sp, color = Color.Gray)
                        Text(AppUtils.formatDate(date), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chọn danh mục (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục") },
                    leadingIcon = { 
                        Icon(
                            AppUtils.getCategoryIcon(selectedCategory), 
                            null,
                            tint = currentTypeColor
                        ) 
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = currentTypeColor,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                        focusedLabelColor = currentTypeColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            },
                            leadingIcon = { 
                                Icon(
                                    AppUtils.getCategoryIcon(category), 
                                    null,
                                    modifier = Modifier.size(20.dp)
                                ) 
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nhập ghi chú
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                leadingIcon = { Icon(Icons.Default.EditNote, null, tint = currentTypeColor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = currentTypeColor,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                    focusedLabelColor = currentTypeColor
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                            viewModel.updateTransaction(transaction) { success ->
                                if (success) {
                                    onBack()
                                }
                            }
                        } else {
                            viewModel.addNewTransaction(transaction) { success ->
                                if (success) {
                                    onBack()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentTypeColor
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (isEditMode) "CẬP NHẬT GIAO DỊCH" else "LƯU GIAO DỊCH", 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}