package com.venkat.expense_tracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE dateMillis BETWEEN :from AND :to ORDER BY dateMillis DESC")
    fun getBetween(from: Long, to: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startOfDay AND dateMillis <= :endOfDay ORDER BY dateMillis DESC")
    fun getForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE dateMillis BETWEEN :from AND :to GROUP BY category")
    fun getCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("SELECT dateMillis, SUM(amount) as total FROM expenses WHERE dateMillis BETWEEN :from AND :to GROUP BY dateMillis ORDER BY dateMillis")
    fun getDailyTotals(from: Long, to: Long): Flow<List<DailyTotal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE title = :title AND amount = :amount AND dateMillis BETWEEN :dayStart AND :dayEnd LIMIT 1")
    suspend fun findDuplicate(title: String, amount: Double, dayStart: Long, dayEnd: Long): Expense?

    @Query("SELECT COUNT(*) FROM expenses WHERE dateMillis BETWEEN :from AND :to")
    suspend fun getExpenseCount(from: Long, to: Long): Int

    @Query("SELECT SUM(amount) FROM expenses WHERE dateMillis BETWEEN :from AND :to")
    suspend fun getTotalAmount(from: Long, to: Long): Double?
}

data class CategoryTotal(
    val category: ExpenseCategory,
    val total: Double
)

data class DailyTotal(
    val dateMillis: Long,
    val total: Double
)