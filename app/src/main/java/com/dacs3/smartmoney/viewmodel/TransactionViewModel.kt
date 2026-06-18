package com.dacs3.smartmoney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dacs3.smartmoney.data.model.Transaction
import com.dacs3.smartmoney.data.model.Budget
import com.dacs3.smartmoney.data.model.Category
import com.dacs3.smartmoney.data.remote.FirebaseSource
import com.dacs3.smartmoney.util.AppUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class TransactionViewModel : ViewModel() {
    private val repository = FirebaseSource()

    // Danh sách giao dịch gốc từ Firebase
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    // Quản lý Danh mục động
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val expenseCategories = _categories.map { list ->
        (AppUtils.EXPENSE_CATEGORIES + list.filter { it.type == "EXPENSE" }.map { it.name }).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUtils.EXPENSE_CATEGORIES)

    val incomeCategories = _categories.map { list ->
        (AppUtils.INCOME_CATEGORIES + list.filter { it.type == "INCOME" }.map { it.name }).distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUtils.INCOME_CATEGORIES)

    // Các trạng thái lọc
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStatsTab = MutableStateFlow(0) // 0: Tuần, 1: Tháng, 2: Năm
    val selectedStatsTab: StateFlow<Int> = _selectedStatsTab.asStateFlow()

    private val _selectedStatsType = MutableStateFlow(1) // 0: Thu nhập, 1: Chi tiêu, 2: Chênh lệch
    val selectedStatsType: StateFlow<Int> = _selectedStatsType.asStateFlow()

    // Danh sách tất cả giao dịch (chỉ sắp xếp, không lọc) - Dùng cho Thống kê
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions
        .map { list -> list.sortedByDescending { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Thống kê theo tuần (7 ngày của tuần hiện tại)
    val weeklyStats: StateFlow<List<Triple<String, Double, Double>>> = _allTransactions.map { list ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Chỉnh về Thứ 2 đầu tuần
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        dayLabels.map { label ->
            val dayStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val dayEnd = calendar.timeInMillis

            val dayTransactions = list.filter { it.date in dayStart until dayEnd }
            val income = dayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = dayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            Triple(label, income, expense)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Thống kê theo tháng (Chia theo tuần trong tháng hiện tại)
    val monthlyStatsList: StateFlow<List<Triple<String, Double, Double>>> = _allTransactions.map { list ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val filtered = list.filter {
            val tCal = Calendar.getInstance().apply { timeInMillis = it.date }
            tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
        }

        (1..5).mapNotNull { week ->
            val startDay = (week - 1) * 7 + 1
            if (startDay > maxDays) return@mapNotNull null
            val endDay = (week * 7).coerceAtMost(maxDays)

            val weekTransactions = filtered.filter {
                val day = Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.DAY_OF_MONTH)
                day in startDay..endDay
            }

            val income = weekTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = weekTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            Triple("$startDay-$endDay", income, expense)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Thống kê theo năm (12 tháng của năm hiện tại)
    val yearlyStatsList: StateFlow<List<Triple<String, Double, Double>>> = _allTransactions.map { list ->
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val filtered = list.filter {
            Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.YEAR) == currentYear
        }

        val months = filtered.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.MONTH) + 1
        }

        (1..12).map { month ->
            val monthTransactions = months[month] ?: emptyList()
            val income = monthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = monthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            Triple("T$month", income, expense)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Thống kê so sánh với kỳ trước
    val statsComparison: StateFlow<Pair<Double, Boolean>> = combine(
        _allTransactions,
        _selectedStatsTab,
        _selectedStatsType
    ) { list, tab, type ->
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val (currentVal, prevVal) = when (tab) {
            0 -> { // Tuần (So sánh tuần này với tuần trước)
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) cal.add(Calendar.DAY_OF_MONTH, -1)
                val thisWeekStart = cal.timeInMillis
                
                val current = list.filter { it.date >= thisWeekStart }
                
                cal.add(Calendar.DAY_OF_MONTH, -7)
                val lastWeekStart = cal.timeInMillis
                val lastWeekEnd = thisWeekStart
                val prev = list.filter { it.date in lastWeekStart until lastWeekEnd }
                
                fun sum(l: List<Transaction>) = when(type) {
                    0 -> l.filter { it.type == "INCOME" }.sumOf { it.amount }
                    1 -> l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    else -> l.filter { it.type == "INCOME" }.sumOf { it.amount } - l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                }
                sum(current) to sum(prev)
            }
            1 -> { // Tháng (So sánh tháng này với tháng trước)
                val current = list.filter {
                    val tCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    tCal.get(Calendar.MONTH) == currentMonth && tCal.get(Calendar.YEAR) == currentYear
                }
                val prev = list.filter {
                    val tCal = Calendar.getInstance().apply { timeInMillis = it.date }
                    val pMonth = if (currentMonth == 0) 11 else currentMonth - 1
                    val pYear = if (currentMonth == 0) currentYear - 1 else currentYear
                    tCal.get(Calendar.MONTH) == pMonth && tCal.get(Calendar.YEAR) == pYear
                }
                fun sum(l: List<Transaction>) = when(type) {
                    0 -> l.filter { it.type == "INCOME" }.sumOf { it.amount }
                    1 -> l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    else -> l.filter { it.type == "INCOME" }.sumOf { it.amount } - l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                }
                sum(current) to sum(prev)
            }
            2 -> { // Năm (So sánh năm nay với năm trước)
                val current = list.filter {
                    Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.YEAR) == currentYear
                }
                val prev = list.filter {
                    Calendar.getInstance().apply { timeInMillis = it.date }.get(Calendar.YEAR) == currentYear - 1
                }
                fun sum(l: List<Transaction>) = when(type) {
                    0 -> l.filter { it.type == "INCOME" }.sumOf { it.amount }
                    1 -> l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    else -> l.filter { it.type == "INCOME" }.sumOf { it.amount } - l.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                }
                sum(current) to sum(prev)
            }
            else -> 0.0 to 0.0
        }
        
        val diff = currentVal - prevVal
        Math.abs(diff) to (diff >= 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0 to true)

    // Danh sách giao dịch sau khi lọc (Tìm kiếm + Danh mục) - Dùng cho Màn hình chính
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _allTransactions,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { transaction ->
            val matchQuery = transaction.note.contains(query, ignoreCase = true) || 
                             transaction.categoryName.contains(query, ignoreCase = true)
            val matchCategory = category == "Tất cả" || transaction.categoryName == category
            
            matchQuery && matchCategory
        }.sortedByDescending { it.date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Danh sách chi tiêu theo tháng để so sánh
    val monthlyExpenseComparison: StateFlow<List<Pair<String, Double>>> = _allTransactions
        .map { list ->
            val calendar = Calendar.getInstance()
            list.filter { it.type == "EXPENSE" }
                .groupBy {
                    calendar.timeInMillis = it.date
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    "T$month/$year"
                }
                .mapValues { it.value.sumOf { t -> t.amount } }
                .toList()
                .sortedWith { a, b ->
                    val aParts = a.first.substring(1).split("/")
                    val bParts = b.first.substring(1).split("/")
                    if (aParts[1] != bParts[1]) aParts[1].compareTo(bParts[1])
                    else aParts[0].toInt().compareTo(bParts[0].toInt())
                }
                .takeLast(6) // Lấy 6 tháng gần nhất
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Quản lý Ngân sách
    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets.asStateFlow()

    // Trạng thái loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var transactionsJob: kotlinx.coroutines.Job? = null
    private var budgetsJob: kotlinx.coroutines.Job? = null
    private var categoriesJob: kotlinx.coroutines.Job? = null

    init {
        reloadAllData()
    }

    // CHỨC NĂNG: LẮNG NGHE DỮ LIỆU THỜI GIAN THỰC
    fun reloadAllData() {
        loadTransactionsRealtime()
        loadBudgetsRealtime()
        loadCategoriesRealtime()
    }

    fun clearData() {
        transactionsJob?.cancel()
        budgetsJob?.cancel()
        categoriesJob?.cancel()
        _allTransactions.value = emptyList()
        _budgets.value = emptyList()
        _categories.value = emptyList()
    }

    fun loadTransactionsRealtime() {
        transactionsJob?.cancel()
        transactionsJob = viewModelScope.launch {
            _isLoading.value = true
            repository.getAllTransactionsRealtime().collect { list ->
                _allTransactions.value = list
                _isLoading.value = false
            }
        }
    }

    fun loadBudgetsRealtime() {
        budgetsJob?.cancel()
        budgetsJob = viewModelScope.launch {
            repository.getBudgetsRealtime().collect { list ->
                _budgets.value = list
            }
        }
    }

    fun loadCategoriesRealtime() {
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            repository.getCombinedCategoriesRealtime().collect { list ->
                _categories.value = list
            }
        }
    }

    fun addCategory(name: String, type: String, iconName: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val category = Category(name = name, type = type, iconName = iconName)
            val result = repository.addCategory(category)
            onComplete(result.isSuccess)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    // Cập nhật query tìm kiếm
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Cập nhật danh mục lọc
    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun onStatsTabChange(tabIndex: Int) {
        _selectedStatsTab.value = tabIndex
    }

    fun onStatsTypeChange(typeIndex: Int) {
        _selectedStatsType.value = typeIndex
    }



    // CHỨC NĂNG: THÊM KHOẢN THU CHI
    fun addNewTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.addTransaction(transaction)
            onComplete(result.isSuccess)
        }
    }

    // CHỨC NĂNG: XÓA KHOẢN THU CHI
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }

    // CHỨC NĂNG: CẬP NHẬT KHOẢN THU CHI
    fun updateTransaction(transaction: Transaction, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateTransaction(transaction)
            onComplete(result.isSuccess)
        }
    }

    // CHỨC NĂNG: LƯU NGÂN SÁCH
    fun saveBudget(budget: Budget, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveBudget(budget)
            onComplete(result.isSuccess)
        }
    }
}