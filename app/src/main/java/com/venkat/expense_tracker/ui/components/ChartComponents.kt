package com.venkat.expense_tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.venkat.expense_tracker.R
import com.venkat.expense_tracker.data.Expense
import com.venkat.expense_tracker.data.ExpenseCategory
import com.venkat.expense_tracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import com.venkat.expense_tracker.ui.theme.Typography

@Composable
fun ExpenseSummaryCard(
    totalExpenses: Double,
    monthlyExpenses: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Expenses",
                style = Typography.titleLarge,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "₹${String.format("%.2f", totalExpenses)}",
                style = Typography.headlineLarge,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This Month",
                style = Typography.titleSmall,
                color = OnSurface
            )
            Text(
                text = "₹${String.format("%.2f", monthlyExpenses)}",
                style = Typography.titleLarge,
                color = Secondary
            )
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Category Breakdown",
                style = typography.titleLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val categoryTotals = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { expense -> expense.amount } }
                .toList()
                .sortedByDescending { it.second }

            categoryTotals.forEach { (category, total) ->
                CategoryRow(
                    category = category,
                    total = total,
                    percentage = (total / categoryTotals.sumOf { it.second }) * 100
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: ExpenseCategory,
    total: Double,
    percentage: Double
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = getCategoryIcon(category),
            contentDescription = null,
            tint = getCategoryColor(category),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = category.displayName,
                style = typography.bodyMedium,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "₹${String.format("%.2f", total)}",
                style = typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }

        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = typography.bodyMedium,
            color = colors.primary,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun ExpensePieChart(expenses: List<Expense>, modifier: Modifier = Modifier) {
    // Group expenses by category
    val categoryTotals = expenses
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    val totalAmount = categoryTotals.values.sum()
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier
            .size(200.dp)
            .padding(8.dp)) {
            var startAngle = -90f
            categoryTotals.entries.forEachIndexed { index, entry ->
                val sweep = if (totalAmount > 0) {
                    (entry.value / totalAmount) * 360f
                } else 0f

                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep.toFloat(),
                    useCenter = true
                )
                startAngle += sweep.toFloat()
            }
        }

        // Legend
        Column(modifier = Modifier.padding(top = 8.dp)) {
            categoryTotals.entries.forEachIndexed { index, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${entry.key}: ₹${String.format("%.2f", entry.value)}")
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    expenses: List<Expense>,
    modifier: Modifier = Modifier
) {
    val monthlyData = expenses
        .groupBy {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(it.dateMillis))
        }
        .toSortedMap() // ✅ ensures month order
        .mapValues { (_, list) -> list.sumOf { it.amount } }

    val maxAmount = monthlyData.values.maxOrNull() ?: 0.0
    val primaryColor = MaterialTheme.colorScheme.primary // ✅ Material 3 color

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp) // ✅ Fixed height so bars are visible
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width / monthlyData.size
            val maxHeight = size.height * 0.8f

            monthlyData.entries.forEachIndexed { index, (month, amount) ->
                val barHeight = if (maxAmount > 0) {
                    (amount / maxAmount) * maxHeight
                } else 0f

                val x = index * barWidth + (barWidth * 0.1f)
                val y = size.height - barHeight.toFloat()

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth * 0.8f, barHeight.toFloat())
                )
            }
        }

        // ✅ Labels below bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            monthlyData.keys.forEach { month ->
                Text(month, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun getCategoryIcon(category: ExpenseCategory) = when (category) {
    ExpenseCategory.FOOD -> painterResource(com.venkat.expense_tracker.R.drawable.food)
    ExpenseCategory.TRAVEL -> painterResource(com.venkat.expense_tracker.R.drawable.travel)
    ExpenseCategory.STAFF -> painterResource(com.venkat.expense_tracker.R.drawable.group)
    ExpenseCategory.UTILITY -> painterResource(com.venkat.expense_tracker.R.drawable.utility)
    ExpenseCategory.OTHER -> painterResource(R.drawable.other)
}

private fun getCategoryColor(category: ExpenseCategory) = when (category) {
    ExpenseCategory.FOOD -> Color(0xFFFF9800)
    ExpenseCategory.TRAVEL -> Color(0xFF2196F3)
    ExpenseCategory.STAFF -> Color(0xFF9C27B0)
    ExpenseCategory.UTILITY -> Color(0xFF4CAF50)
    ExpenseCategory.OTHER -> Color(0xFF607D8B)
} 