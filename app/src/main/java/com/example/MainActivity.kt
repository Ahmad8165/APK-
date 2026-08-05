package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddEditCustomerDialog
import com.example.ui.components.AppLockDialog
import com.example.ui.components.CustomerDetailDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.PaymentHistoryDialog
import com.example.ui.screens.*
import com.example.ui.theme.DolphinFiberTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CustomerFilter
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.settingsManager.isDarkMode.collectAsState()

            DolphinFiberTheme(darkTheme = isDarkMode) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isAppLocked by viewModel.isAppLocked.collectAsState()
                val companyName by viewModel.settingsManager.companyName.collectAsState()
                val currency by viewModel.settingsManager.currency.collectAsState()

                val dashboardMetrics by viewModel.dashboardMetrics.collectAsState()
                val customerForDetail by viewModel.customerForDetail.collectAsState()
                val customerForEdit by viewModel.customerForEdit.collectAsState()
                val isAddDialogOpen by viewModel.isAddCustomerDialogOpen.collectAsState()
                val customerForPayments by viewModel.customerForPayments.collectAsState()
                val customerToDelete by viewModel.customerToDelete.collectAsState()

                if (currentScreen == AppScreen.SPLASH) {
                    SplashScreen(
                        onSplashFinished = {
                            viewModel.navigateTo(AppScreen.DASHBOARD)
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Column {
                                            Text(
                                                text = companyName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text = "Personal ISP Fee Management",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = { viewModel.settingsManager.setDarkMode(!isDarkMode) },
                                            modifier = Modifier.testTag("theme_toggle_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                                contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode"
                                            )
                                        }
                                        IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                                            Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                                        }
                                        IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }) {
                                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("bottom_nav_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.DASHBOARD,
                                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                        label = { Text("Dashboard") },
                                        modifier = Modifier.testTag("nav_dashboard")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.CUSTOMERS,
                                        onClick = { viewModel.navigateTo(AppScreen.CUSTOMERS) },
                                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                                        label = { Text("Customers") },
                                        modifier = Modifier.testTag("nav_customers")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.REPORTS,
                                        onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                                        label = { Text("Reports") },
                                        modifier = Modifier.testTag("nav_reports")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.BACKUP,
                                        onClick = { viewModel.navigateTo(AppScreen.BACKUP) },
                                        icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                                        label = { Text("Backup") },
                                        modifier = Modifier.testTag("nav_backup")
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == AppScreen.SETTINGS,
                                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        label = { Text("Settings") },
                                        modifier = Modifier.testTag("nav_settings")
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                AnimatedContent(
                                    targetState = currentScreen,
                                    label = "ScreenTransition"
                                ) { screen ->
                                    when (screen) {
                                        AppScreen.SPLASH -> {}
                                        AppScreen.DASHBOARD -> DashboardScreen(
                                            viewModel = viewModel,
                                            metrics = dashboardMetrics,
                                            currency = currency,
                                            onNavigateToCustomers = { filter ->
                                                viewModel.setFilter(filter)
                                                viewModel.navigateTo(AppScreen.CUSTOMERS)
                                            },
                                            onAddCustomer = {
                                                viewModel.showEditCustomer(null)
                                                viewModel.isAddCustomerDialogOpen.value = true
                                            }
                                        )
                                        AppScreen.CUSTOMERS -> CustomerListScreen(
                                            viewModel = viewModel,
                                            currency = currency,
                                            onAddCustomer = {
                                                viewModel.showEditCustomer(null)
                                                viewModel.isAddCustomerDialogOpen.value = true
                                            }
                                        )
                                        AppScreen.REPORTS -> ReportsScreen(
                                            viewModel = viewModel,
                                            metrics = dashboardMetrics,
                                            currency = currency
                                        )
                                        AppScreen.BACKUP -> BackupScreen(viewModel = viewModel)
                                        AppScreen.SETTINGS -> SettingsScreen(
                                            viewModel = viewModel,
                                            settingsManager = viewModel.settingsManager
                                        )
                                    }
                                }
                            }
                        }

                        // App Lock Overlay
                        if (isAppLocked) {
                            AppLockDialog(
                                onUnlock = { pin ->
                                    viewModel.validatePin(pin)
                                }
                            )
                        }

                        // Customer Details Dialog
                        customerForDetail?.let { customer ->
                            CustomerDetailDialog(
                                customer = customer,
                                currency = currency,
                                onDismiss = { viewModel.showCustomerDetail(null) },
                                onEdit = {
                                    viewModel.showCustomerDetail(null)
                                    viewModel.showEditCustomer(customer)
                                    viewModel.isAddCustomerDialogOpen.value = true
                                },
                                onDelete = { viewModel.showDeleteConfirmation(customer) },
                                onTogglePayment = { viewModel.togglePaymentStatus(customer) },
                                onToggleSuspend = {
                                    if (customer.isSuspended) viewModel.activateCustomer(customer)
                                    else viewModel.suspendCustomer(customer)
                                },
                                onViewHistory = { viewModel.showPaymentsForCustomer(customer) }
                            )
                        }

                        // Delete Confirmation Dialog
                        customerToDelete?.let { customer ->
                            DeleteConfirmationDialog(
                                customer = customer,
                                onConfirm = { viewModel.confirmDeleteCustomer() },
                                onDismiss = { viewModel.showDeleteConfirmation(null) }
                            )
                        }

                        // Add/Edit Customer Dialog
                        if (isAddDialogOpen) {
                            AddEditCustomerDialog(
                                customer = customerForEdit,
                                currency = currency,
                                onDismiss = { viewModel.isAddCustomerDialogOpen.value = false },
                                onSave = { updatedCustomer ->
                                    viewModel.saveCustomer(updatedCustomer)
                                }
                            )
                        }

                        // Payment History Dialog
                        customerForPayments?.let { customer ->
                            val payments by viewModel.getPaymentsForCustomerFlow(customer.id)
                                .collectAsState(initial = emptyList())
                            PaymentHistoryDialog(
                                customer = customer,
                                payments = payments,
                                currency = currency,
                                onDismiss = { viewModel.showPaymentsForCustomer(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
