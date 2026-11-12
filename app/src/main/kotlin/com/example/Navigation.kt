package com.example

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ErpRepository
import com.example.ui.components.VoiceAssistantSheet
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.clients.ClientsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.ExpensesScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.invoices.CreateInvoiceScreen
import com.example.ui.screens.invoices.InvoiceDetailScreen
import com.example.ui.screens.invoices.InvoicesScreen
import com.example.ui.screens.reports.TaxReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.ErpAmberAccent
import com.example.ui.theme.ErpTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val repository = remember { ErpRepository.instance }
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Dashboard

    var showVoiceAssistant by remember { mutableStateOf(false) }

    fun navigateTo(screen: Screen) {
        if (screen != currentScreen) {
            screenStack.add(screen)
        }
    }

    fun popScreen(): Boolean {
        return if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            true
        } else false
    }

    BackHandler(enabled = screenStack.size > 1) {
        popScreen()
    }

    val isTopLevel = when (currentScreen) {
        Screen.Dashboard, Screen.Invoices, Screen.Inventory, Screen.Clients, Screen.Settings -> true
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = ErpTealPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("ERP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                            }
                        }
                        Column {
                            Text(
                                text = when (currentScreen) {
                                    Screen.Dashboard -> "My Business ERP"
                                    Screen.Invoices -> "Invoices & Billing"
                                    Screen.Inventory -> "Inventory & Stock"
                                    Screen.Clients -> "Customer Directory"
                                    Screen.Expenses -> "Expense Manager"
                                    Screen.TaxReports -> "CA Tax Portal (GSTR-1)"
                                    Screen.Catalog -> "Catalog Studio"
                                    Screen.Settings -> "Business Settings"
                                    is Screen.InvoiceDetail -> "Invoice View"
                                    Screen.CreateInvoice -> "New Tax Invoice"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { popScreen() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { showVoiceAssistant = true },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = ErpTealPrimary.copy(alpha = 0.12f),
                            contentColor = ErpTealPrimary
                        ),
                        modifier = Modifier.testTag("topbar_voice_mic_btn")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Assistant")
                    }

                    if (isTopLevel) {
                        IconButton(
                            onClick = { navigateTo(Screen.Settings) },
                            modifier = Modifier.testTag("topbar_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Dashboard,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Dashboard)
                        },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Invoices,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Invoices)
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Invoices") },
                        label = { Text("Invoices", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_invoices")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Inventory,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Inventory)
                        },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventory") },
                        label = { Text("Stock", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_inventory")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Clients,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Clients)
                        },
                        icon = { Icon(Icons.Default.People, contentDescription = "Clients") },
                        label = { Text("Clients", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_clients")
                    )
                    NavigationBarItem(
                        selected = currentScreen is Screen.Settings,
                        onClick = {
                            screenStack.clear()
                            screenStack.add(Screen.Settings)
                        },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Settings") },
                        label = { Text("More", fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val screen = currentScreen) {
                Screen.Dashboard -> {
                    DashboardScreen(
                        repository = repository,
                        onNavigate = { route ->
                            when (route) {
                                "invoices" -> navigateTo(Screen.Invoices)
                                "inventory" -> navigateTo(Screen.Inventory)
                                "clients" -> navigateTo(Screen.Clients)
                                "expenses" -> navigateTo(Screen.Expenses)
                                "reports" -> navigateTo(Screen.TaxReports)
                                "catalog" -> navigateTo(Screen.Catalog)
                                "create_invoice" -> navigateTo(Screen.CreateInvoice)
                                "settings" -> navigateTo(Screen.Settings)
                                else -> {}
                            }
                        },
                        onSelectInvoice = { invId -> navigateTo(Screen.InvoiceDetail(invId)) },
                        onOpenVoiceAssistant = { showVoiceAssistant = true }
                    )
                }

                Screen.Invoices -> {
                    InvoicesScreen(
                        repository = repository,
                        onSelectInvoice = { invId -> navigateTo(Screen.InvoiceDetail(invId)) },
                        onCreateInvoice = { navigateTo(Screen.CreateInvoice) }
                    )
                }

                Screen.Inventory -> {
                    InventoryScreen(
                        repository = repository,
                        onOpenVoiceAssistant = { showVoiceAssistant = true }
                    )
                }

                Screen.Clients -> {
                    ClientsScreen(
                        repository = repository,
                        onSelectClientInvoices = { clientId -> navigateTo(Screen.Invoices) }
                    )
                }

                Screen.Expenses -> {
                    ExpensesScreen(
                        repository = repository
                    )
                }

                Screen.TaxReports -> {
                    TaxReportsScreen(
                        repository = repository
                    )
                }

                Screen.Catalog -> {
                    CatalogScreen(
                        repository = repository
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        repository = repository
                    )
                }

                is Screen.InvoiceDetail -> {
                    InvoiceDetailScreen(
                        invoiceId = screen.invoiceId,
                        repository = repository,
                        onBack = { popScreen() }
                    )
                }

                Screen.CreateInvoice -> {
                    CreateInvoiceScreen(
                        repository = repository,
                        onBack = { popScreen() },
                        onInvoiceCreated = { invId ->
                            popScreen()
                            navigateTo(Screen.InvoiceDetail(invId))
                        }
                    )
                }
            }
        }
    }

    if (showVoiceAssistant) {
        VoiceAssistantSheet(
            repository = repository,
            onNavigate = { target ->
                when (target) {
                    "dashboard" -> { screenStack.clear(); screenStack.add(Screen.Dashboard) }
                    "invoices" -> { screenStack.clear(); screenStack.add(Screen.Invoices) }
                    "inventory" -> { screenStack.clear(); screenStack.add(Screen.Inventory) }
                    "clients" -> { screenStack.clear(); screenStack.add(Screen.Clients) }
                    "expenses" -> navigateTo(Screen.Expenses)
                    "reports" -> navigateTo(Screen.TaxReports)
                    "catalog" -> navigateTo(Screen.Catalog)
                    "create_invoice" -> navigateTo(Screen.CreateInvoice)
                    else -> {}
                }
            },
            onDismiss = { showVoiceAssistant = false }
        )
    }
}
