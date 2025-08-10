package com.venkat.expense_tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.venkat.expense_tracker.viewmodel.ExpenseViewModel
import com.venkat.expense_tracker.ui.screens.AddExpenseScreen
import com.venkat.expense_tracker.ui.screens.ListScreen
import com.venkat.expense_tracker.ui.screens.ReportScreen

@Composable
fun ExpenseNavHost(viewModel: ExpenseViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "list") {
        composable("list") {
            ListScreen(
                viewModel = viewModel, 
                onNavigateToAdd = { nav.navigate("add") }, 
                onNavigateToReport = { nav.navigate("report") }
            )
        }
        composable("add") {
            AddExpenseScreen(
                viewModel = viewModel, 
                onBack = { nav.popBackStack() }
            )
        }
        composable("report") {
            ReportScreen(

                viewModel = viewModel, 
                onBack = { nav.popBackStack() }
            )
        }
    }
}