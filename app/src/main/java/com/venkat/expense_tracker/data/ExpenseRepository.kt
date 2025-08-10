package com.venkat.expense_tracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(private val dao: ExpenseDao) {

    fun getAll(): Flow<List<Expense>> = dao.getAll()

    fun getBetween(from: Long, to: Long): Flow<List<Expense>> = dao.getBetween(from, to)

    fun getForDay(startOfDay: Long, endOfDay: Long): Flow<List<Expense>> = dao.getForDay(startOfDay, endOfDay)

    fun getCategoryTotals(from: Long, to: Long): Flow<List<CategoryTotal>> = dao.getCategoryTotals(from, to)

    fun getDailyTotals(from: Long, to: Long): Flow<List<DailyTotal>> = dao.getDailyTotals(from, to)

    suspend fun addExpense(expense: Expense): Long = withContext(Dispatchers.IO) {
        dao.insert(expense)
    }

    suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.delete(expense)
    }

    suspend fun findDuplicate(title: String, amount: Double, dayStart: Long, dayEnd: Long): Expense? {
        return dao.findDuplicate(title, amount, dayStart, dayEnd)
    }

    suspend fun getExpenseCount(from: Long, to: Long): Int = withContext(Dispatchers.IO) {
        dao.getExpenseCount(from, to)
    }

    suspend fun getTotalAmount(from: Long, to: Long): Double = withContext(Dispatchers.IO) {
        dao.getTotalAmount(from, to) ?: 0.0
    }

    suspend fun getFilteredExpenses(days: Int): List<Expense> = withContext(Dispatchers.IO) {
        val (start, end) = getDateRangeForLastDays(days)
        dao.getBetween(start, end).first()
    }

    // Enhanced CSV export with better formatting
    fun toCsv(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("ID,Title,Amount (₹),Category,Note,Date,Time,Receipt URI\n")
        for (expense in expenses) {
            sb.append("${expense.id},")
            sb.append("\"${expense.title}\",")
            sb.append("${expense.amount},")
            sb.append("${expense.category.displayName},")
            sb.append("\"${expense.note ?: ""}\",")
            sb.append("${expense.getFormattedDate()},")
            sb.append("${expense.getFormattedTime()},")
            sb.append("${expense.receiptUri ?: ""}\n")
        }
        return sb.toString()
    }

    fun exportAndSharePdf(context: Context, expenses: List<Expense>, fileName: String = "expenses.pdf") {
        val pdfDocument = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }

        // A4-ish dimensions in points (approx)
        val pageWidth = 595
        val pageHeight = 842
        val leftMargin = 20f
        val topMargin = 40f
        val rowHeight = 20f
        val bottomMargin = 40f

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        fun drawHeader(canvas: Canvas, paint: Paint, yStart: Float): Float {
            paint.textSize = 16f
            paint.isFakeBoldText = true
            canvas.drawText("Expense Report", leftMargin, yStart, paint)
            paint.textSize = 12f
            paint.isFakeBoldText = false
            var y = yStart + 25f

            // Column headers
            val headers = listOf("ID", "Title", "Amount ₹", "Category", "Date", "Time")
            headers.forEachIndexed { index, header ->
                canvas.drawText(header, leftMargin + index * 90f, y, paint)
            }
            y += 12f
            canvas.drawLine(leftMargin, y, pageWidth - leftMargin, y, paint)
            return y + 12f
        }

        var y = drawHeader(canvas, paint, topMargin)

        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            for ((rowIndex, expense) in expenses.withIndex()) {
                // If next row won't fit, finish page and start a new one
                if (y + rowHeight > pageHeight - bottomMargin) {
                    pdfDocument.finishPage(page)

                    pageNumber += 1
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    // Draw header on new page and reset y
                    y = drawHeader(canvas, paint, topMargin)
                }

                val cells = listOf(
                    expense.id.toString(),
                    expense.title.take(30), // limit length to avoid overflow
                    String.format("%.2f", expense.amount),
                    expense.category.displayName,
                    expense.getFormattedDate(),  // or dateFmt.format(Date(expense.dateMillis))
                    expense.getFormattedTime()   // or timeFmt.format(Date(expense.dateMillis))
                )

                cells.forEachIndexed { idx, text ->
                    canvas.drawText(text, leftMargin + idx * 90f, y, paint)
                }
                y += rowHeight
            }

            // finish last page
            pdfDocument.finishPage(page)

            // Save to file in app external files dir
            val outFile = File(context.getExternalFilesDir(null), fileName)
            FileOutputStream(outFile).use { fos ->
                pdfDocument.writeTo(fos)
            }

            // Close document
            pdfDocument.close()

            // Share using FileProvider
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", outFile)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF via"))

        } catch (e: Exception) {
            // Clean up
            try { pdfDocument.close() } catch (_: Exception) {}
            e.printStackTrace()
            // Optionally report the error to the user (Toast) from the caller
        }
    }


    // Get start and end of day for date filtering
    fun getStartOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfDay(dateMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // Get date range for last N days
    fun getDateRangeForLastDays(days: Int): Pair<Long, Long> {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance()
        start.add(Calendar.DAY_OF_MONTH, -days + 1)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)
        return Pair(start.timeInMillis, end.timeInMillis)
    }
}