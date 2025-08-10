package com.venkat.expense_tracker.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.venkat.expense_tracker.data.Expense
import com.venkat.expense_tracker.data.ExpenseCategory
import com.venkat.expense_tracker.viewmodel.ExpenseViewModel
import com.venkat.expense_tracker.ui.state.ExpenseEvent
import com.venkat.expense_tracker.ui.state.ExportFormat
import com.venkat.expense_tracker.ui.components.*
import com.venkat.expense_tracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: ExpenseViewModel, 
    onNavigateToAdd: () -> Unit, 
    onNavigateToReport: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Smart Expense Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.handleEvent(ctx,
                            ExpenseEvent.ToggleTheme
                        ) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(34.dp),
                            painter = if (state.themeIsDark) painterResource(id = R.drawable.ic_light) else painterResource(id = R.drawable.dark),
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onNavigateToReport,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource( R.drawable.report),
                            contentDescription = "Reports",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            // Today's Summary Card with enhanced styling
            StatCard(
                title = "Today's Total",
                value = "₹${String.format("%.2f", state.todayTotal)}",
                subtitle = "${state.totalExpenses} expenses today"
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Filter Options with improved layout
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Filter by days:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf(7, 15, 30).forEach { days ->
                            CustomFilterChip(
                                onClick = { viewModel.handleEvent(ctx,ExpenseEvent.SetFilterDays(days)) },
                                label = "$days days" ,
                                selected = state.filterDays == days
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Group by category toggle with enhanced styling
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Group by category:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Switch(
                            checked = state.groupByCategory,
                            onCheckedChange = { viewModel.handleEvent(ctx,ExpenseEvent.ToggleGroupByCategory) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Dummy Data Button (for testing) with enhanced styling
            if (state.filteredExpenses.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                ) {
                    Button(
                        onClick = { viewModel.populateDummyData() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Load Sample Data",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (state.isLoading) {
                LoadingSpinner()
            } else if (state.filteredExpenses.isEmpty()) {
                EmptyState("No expenses found for the selected period. Tap + to add a new expense.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.groupByCategory) {
                        // Group expenses by category
                        val groupedExpenses = state.filteredExpenses.groupBy { it.category }
                        groupedExpenses.forEach { (category, expenses) ->
                            item {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = if (isSystemInDarkTheme()) 0.4f else 0.25f
                                        )
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${category.icon}  ${category.displayName} (${expenses.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }

                            }
                            items(expenses) { expense ->
                                ExpenseCard(
                                    expense = expense,
                                    onDelete = { 
                                        viewModel.handleEvent(ctx,ExpenseEvent.DeleteExpense(expense))
                                        Toast.makeText(ctx, "Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    } else {
                        // Show expenses chronologically
                        items(state.filteredExpenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                onDelete = { 
                                    viewModel.handleEvent(ctx,ExpenseEvent.DeleteExpense(expense))
                                    Toast.makeText(ctx, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(ctx, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel, 
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var note by remember { mutableStateOf("") }
    var receiptUri by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        receiptUri = uri?.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add New Expense",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (title.isBlank()) {
                        Toast.makeText(ctx, "Title required", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }
                    if (amt <= 0.0) {
                        Toast.makeText(ctx, "Amount > 0 required", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }
                    
                    val expense = Expense(
                        title = title,
                        amount = amt,
                        category = category,
                        note = note.takeIf { it.isNotBlank() },
                        dateMillis = System.currentTimeMillis(),
                        receiptUri = receiptUri
                    )
                    
                    viewModel.handleEvent(ctx,ExpenseEvent.AddExpense(expense))
                    onBack()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    Icons.Default.Check, 
                    contentDescription = "Save",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
            
            item {
                // Amount Field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
            
            item {
                // Category Selection with enhanced styling
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Category:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(ExpenseCategory.values()) { cat ->
                                CustomFilterChip(
                                    onClick = { category = cat },
                                    label = "${cat.icon} ${cat.displayName}",
                                    selected = category == cat
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Note Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(100) },
                    label = { Text("Note (optional, max 100 chars)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )
            }
            
            item {
                // Receipt Image Section
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Receipt Image",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { launcher.launch("image/*") },
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Pick Receipt Image")
                            }
                            
                            Spacer(modifier = Modifier.width(20.dp))
                            
                            if (receiptUri != null) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Image selected",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Preview selected image with enhanced styling
                        receiptUri?.let { uri ->
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                AsyncImage(
                                    model = Uri.parse(uri),
                                    contentDescription = "Receipt preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val ctx = LocalContext.current

    // Filter expenses based on selected period
    val filteredExpenses = remember(state.expenses, state.filterDays) {
        val now = System.currentTimeMillis()
        val startTime = now - state.filterDays * 24 * 60 * 60 * 1000L
        state.expenses.filter { it.dateMillis >= startTime }
    }

    // Dynamic totals for filtered list
    val totalExpenses = filteredExpenses.size
    val totalAmount = filteredExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Expense Reports",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Totals
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    StatCard(
                        title = "Total Expenses",
                        value = "$totalExpenses",
                        subtitle = "Last ${state.filterDays} days",
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Total Amount",
                        value = "₹${String.format("%.2f", totalAmount)}",
                        subtitle = "Last ${state.filterDays} days",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter chips
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Time Period:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(7, 15, 30).forEach { days ->
                                CustomFilterChip(
                                    onClick = { viewModel.handleEvent(ctx,ExpenseEvent.SetFilterDays(days)) },
                                    label = "$days days",
                                    selected = state.filterDays == days
                                )
                            }
                        }
                    }
                }
            }

            // Loading state
            if (state.isLoading) {
                item { LoadingSpinner() }
            } else {
                // Charts
                item { BarChart(data = filteredExpenses) }
                item { PieChart(filteredExpenses) }
                item { CategoryBreakdownCard(filteredExpenses) }

                // Export buttons
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.padding(6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Export Data:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.handleEvent(ctx,ExpenseEvent.ExportData(context = ctx,ExportFormat.CSV)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Export CSV")
                                }

                                FilledTonalButton(
                                    onClick = { viewModel.handleEvent(ctx,ExpenseEvent.ExportData(ctx,ExportFormat.PDF)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Export PDF")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Error handling
    state.error?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(ctx, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

