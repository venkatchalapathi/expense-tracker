package com.venkat.expense_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.venkat.expense_tracker.ui.theme.AppTheme
import com.venkat.expense_tracker.ui.navigation.ExpenseNavHost
import com.venkat.expense_tracker.viewmodel.ExpenseViewModel
import com.venkat.expense_tracker.data.ExpenseDatabase
import com.venkat.expense_tracker.data.ExpenseRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create DB & repo
        val db = Room.databaseBuilder(
            applicationContext, 
            ExpenseDatabase::class.java, 
            "expense_db"
        )
            .fallbackToDestructiveMigration()
            .build()
        


        setContent {
            val repo = ExpenseRepository(db.expenseDao())
            val viewModel: ExpenseViewModel =
                viewModel(factory = ExpenseViewModel.provideFactory(repo))
            val state by viewModel.state.collectAsState()
            AppTheme(darkTheme = state.themeIsDark) {
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.provideFactory(repo)
                )
                ExpenseNavHost(viewModel = viewModel)
            }
        }
    }
}
