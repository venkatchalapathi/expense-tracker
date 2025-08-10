package com.venkat.expense_tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

enum class ExpenseCategory(val displayName: String, val icon: String) {
    STAFF("Staff", "👥"),
    TRAVEL("Travel", "✈️"),
    FOOD("Food", "🍽️"),
    UTILITY("Utility", "⚡"),
    OTHER("Other", "📦")
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val note: String?,
    val dateMillis: Long,
    val receiptUri: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && amount > 0.0
    }
    
    fun getFormattedAmount(): String {
        return "₹${String.format("%.2f", amount)}"
    }
    
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        return "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }
    
    fun getFormattedTime(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }
}