package com.venkat.expense_tracker.ui.state

import android.content.Context
import com.venkat.expense_tracker.data.Expense
import com.venkat.expense_tracker.data.CategoryTotal
import com.venkat.expense_tracker.data.DailyTotal

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val dailyTotals: List<DailyTotal> = emptyList(),
    val filterDays: Int = 7,
    val selectedDate: Long = System.currentTimeMillis(),
    val groupByCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val todayTotal: Double = 0.0,
    val totalExpenses: Int = 0,
    val totalAmount: Double = 0.0,
    val themeIsDark: Boolean = false
)

sealed class ExpenseEvent {
    object LoadExpenses : ExpenseEvent()
    object ToggleTheme : ExpenseEvent()
    data class SetFilterDays(val days: Int) : ExpenseEvent()
    data class SetSelectedDate(val date: Long) : ExpenseEvent()
    object ToggleGroupByCategory : ExpenseEvent()
    data class AddExpense(val expense: Expense) : ExpenseEvent()
    data class DeleteExpense(val expense: Expense) : ExpenseEvent()
    data class ExportData(val context: Context, val format: ExportFormat) : ExpenseEvent()
}

enum class ExportFormat {
    CSV, PDF
}

sealed class ExpenseResult {
    object Success : ExpenseResult()
    data class Error(val message: String) : ExpenseResult()
    data class DuplicateDetected(val message: String) : ExpenseResult()
} 