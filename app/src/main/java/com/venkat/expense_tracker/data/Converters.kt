package com.venkat.expense_tracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory): String {
        return category.name
    }

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory {
        return try {
            ExpenseCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ExpenseCategory.OTHER
        }
    }
} 