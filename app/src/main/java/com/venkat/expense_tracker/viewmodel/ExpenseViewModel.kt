package com.venkat.expense_tracker.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.venkat.expense_tracker.data.Expense
import com.venkat.expense_tracker.data.ExpenseCategory
import com.venkat.expense_tracker.data.ExpenseRepository
import com.venkat.expense_tracker.ui.state.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(private val repo: ExpenseRepository) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseUiState())
    val state: StateFlow<ExpenseUiState> = _state.asStateFlow()

    init {
        loadExpenses()
    }

    fun handleEvent(context: Context,event: ExpenseEvent) {
        when (event) {
            is ExpenseEvent.LoadExpenses -> loadExpenses()
            is ExpenseEvent.ToggleTheme -> toggleTheme()
            is ExpenseEvent.SetFilterDays -> setFilterDays(event.days)
            is ExpenseEvent.SetSelectedDate -> setSelectedDate(event.date)
            is ExpenseEvent.ToggleGroupByCategory -> toggleGroupByCategory()
            is ExpenseEvent.AddExpense -> addExpense(event.expense)
            is ExpenseEvent.DeleteExpense -> deleteExpense(event.expense)
            is ExpenseEvent.ExportData -> exportData(context  = context ,event.format)
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Collect all expenses
                repo.getAll().collect { expenses ->
                    val todayTotal = computeTodayTotal(expenses)
                    val (start, end) = repo.getDateRangeForLastDays(_state.value.filterDays)
                    
                    // Get filtered data
                    val filteredExpenses = repo.getBetween(start, end).first()
                    val categoryTotals = repo.getCategoryTotals(start, end).first()
                    val dailyTotals = repo.getDailyTotals(start, end).first()
                    
                    val totalAmount = repo.getTotalAmount(start, end)
                    val totalExpenses = repo.getExpenseCount(start, end)
                    
                    _state.update { 
                        it.copy(
                            expenses = expenses,
                            filteredExpenses = filteredExpenses,
                            categoryTotals = categoryTotals,
                            dailyTotals = dailyTotals,
                            todayTotal = todayTotal,
                            totalAmount = totalAmount,
                            totalExpenses = totalExpenses,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun computeTodayTotal(expenses: List<Expense>): Double {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val startOfDay = today.timeInMillis
        
        today.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = today.timeInMillis - 1
        
        return expenses.filter { it.dateMillis in startOfDay..endOfDay }
            .sumOf { it.amount }
    }

    fun toggleTheme() {
        _state.update { it.copy(themeIsDark = !it.themeIsDark) }
    }

    private fun setFilterDays(days: Int) {
        _state.update { it.copy(filterDays = days) }
        loadExpenses() // Reload with new filter
    }

    private fun setSelectedDate(date: Long) {
        _state.update { it.copy(selectedDate = date) }
        loadExpenses() // Reload with new date
    }

    private fun toggleGroupByCategory() {
        _state.update { it.copy(groupByCategory = !it.groupByCategory) }
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                // Validation
                if (!expense.isValid()) {
                    _state.update { 
                        it.copy(error = "Invalid expense data. Title and amount are required.")
                    }
                    return@launch
                }

                // Duplicate check: same title+amount on same day
                val startOfDay = repo.getStartOfDay(expense.dateMillis)
                val endOfDay = repo.getEndOfDay(expense.dateMillis)
                val duplicate = repo.findDuplicate(expense.title, expense.amount, startOfDay, endOfDay)
                
                if (duplicate != null) {
                    _state.update { 
                        it.copy(error = "Duplicate expense detected (same title & amount today).")
                    }
                    return@launch
                }

                // Add expense
                repo.addExpense(expense)
                _state.update { it.copy(error = null) }
                
                // Reload expenses
                loadExpenses()
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to add expense")
                }
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repo.deleteExpense(expense)
                loadExpenses() // Reload expenses
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to delete expense")
                }
            }
        }
    }

    private fun exportData(context: Context, format: ExportFormat) {
        viewModelScope.launch {
            try {
                when (format) {
                    ExportFormat.CSV -> {
                        val csvData = repo.toCsv(_state.value.filteredExpenses)
                        // In a real app, you would save this to a file and share it
                        _state.update { 
                            it.copy(error = "CSV exported successfully (${csvData.length} characters)")
                        }
                    }
                    ExportFormat.PDF -> {
                        // Placeholder for PDF export
                        val file = repo.exportAndSharePdf(context, state.value.filteredExpenses, "fileName")
//                        Toast.makeText(context, "PDF saved: ${file}", Toast.LENGTH_LONG).show()
                        _state.update { 
                            it.copy(error = "PDF export feature coming soon!")
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Export failed")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun populateDummyData() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val dummyExpenses = mutableListOf<Expense>()
                
                // Today's expenses
                dummyExpenses.add(Expense(
                    title = "Office Lunch",
                    amount = 450.0,
                    category = ExpenseCategory.FOOD,
                    note = "Team lunch at nearby restaurant",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Uber to Client Meeting",
                    amount = 280.0,
                    category = ExpenseCategory.TRAVEL,
                    note = "Transportation to client office",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // Yesterday's expenses
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Internet Bill",
                    amount = 1200.0,
                    category = ExpenseCategory.UTILITY,
                    note = "Monthly broadband payment",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Coffee for Team",
                    amount = 180.0,
                    category = ExpenseCategory.FOOD,
                    note = "Morning coffee break",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 2 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Staff Training Materials",
                    amount = 850.0,
                    category = ExpenseCategory.STAFF,
                    note = "Training books and materials",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Client Dinner",
                    amount = 1200.0,
                    category = ExpenseCategory.FOOD,
                    note = "Business dinner with client",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 3 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Flight to Mumbai",
                    amount = 4500.0,
                    category = ExpenseCategory.TRAVEL,
                    note = "Business trip to Mumbai",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Hotel Booking",
                    amount = 3200.0,
                    category = ExpenseCategory.TRAVEL,
                    note = "Accommodation in Mumbai",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 4 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Electricity Bill",
                    amount = 1800.0,
                    category = ExpenseCategory.UTILITY,
                    note = "Office electricity payment",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Team Building Activity",
                    amount = 1500.0,
                    category = ExpenseCategory.STAFF,
                    note = "Team outing and activities",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 5 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Office Supplies",
                    amount = 650.0,
                    category = ExpenseCategory.OTHER,
                    note = "Stationery and office items",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Local Transport",
                    amount = 120.0,
                    category = ExpenseCategory.TRAVEL,
                    note = "Local commuting expenses",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 6 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Employee Bonus",
                    amount = 5000.0,
                    category = ExpenseCategory.STAFF,
                    note = "Performance bonus payment",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Water Bill",
                    amount = 450.0,
                    category = ExpenseCategory.UTILITY,
                    note = "Monthly water supply payment",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // 7 days ago
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                dummyExpenses.add(Expense(
                    title = "Marketing Materials",
                    amount = 950.0,
                    category = ExpenseCategory.OTHER,
                    note = "Brochures and promotional items",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                dummyExpenses.add(Expense(
                    title = "Client Lunch",
                    amount = 800.0,
                    category = ExpenseCategory.FOOD,
                    note = "Lunch meeting with potential client",
                    dateMillis = calendar.timeInMillis,
                    receiptUri = null
                ))
                
                // Add all dummy expenses to database
                dummyExpenses.forEach { expense ->
                    repo.addExpense(expense)
                }
                
                // Reload expenses to show the new data
                loadExpenses()
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "Failed to populate dummy data: ${e.message}")
                }
            }
        }
    }

    companion object {
        fun provideFactory(repo: ExpenseRepository): ViewModelProvider.Factory = 
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ExpenseViewModel(repo) as T
                }
            }
    }
}