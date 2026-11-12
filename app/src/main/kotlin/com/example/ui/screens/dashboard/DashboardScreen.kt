package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ErpRepository
import com.example.model.PaymentMode
import com.example.model.PaymentStatus
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.ErpAmberAccent
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpRedDanger
import com.example.ui.theme.ErpTealLight
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    repository: ErpRepository,
    onNavigate: (String) -> Unit,
    onSelectInvoice: (String) -> Unit,
    onOpenVoiceAssistant: () -> Unit
) {
    val businessProfile by repository.businessProfile.collectAsState()
    val invoices by repository.invoices.collectAsState()
    val products by repository.products.collectAsState()
    val clients by repository.clients.collectAsState()
    val expenses by repository.expenses.collectAsState()

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    // Computations
    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayInvoices = invoices.filter { it.date >= startOfToday }
    val todayRevenue = todayInvoices.sumOf { it.amountPaid }
    val todayCash = todayInvoices.filter { it.paymentMode == PaymentMode.CASH }.sumOf { it.amountPaid }
    val todayUpi = todayInvoices.filter { it.paymentMode == PaymentMode.UPI }.sumOf { it.amountPaid }

    val lowStockCount = products.count { it.isLowStock || it.isOutOfStock }
    val totalOutstanding = invoices.sumOf { it.balanceDue }
    val totalTaxCollected = invoices.sumOf { it.totalTax }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ErpTealPrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = businessProfile.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = businessProfile.tagline,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            if (businessProfile.gstin.isNotBlank()) {
                                Text(
                                    text = "GSTIN: ${businessProfile.gstin}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = onOpenVoiceAssistant,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("voice_assistant_quick_btn")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Assistant")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Collection Highlight Box
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TODAY'S COLLECTION",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = inrFormat.format(todayRevenue),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("💵 Cash: ${inrFormat.format(todayCash)}", color = Color.White.copy(alpha = 0.9f), fontSize = 11.5.sp)
                                    Text("📱 UPI: ${inrFormat.format(todayUpi)}", color = Color.White.copy(alpha = 0.9f), fontSize = 11.5.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${todayInvoices.size}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bills today",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Low stock warning banner (if any)
        if (lowStockCount > 0) {
            item {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("inventory") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stock Alert",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stock Alert: $lowStockCount items low or out of stock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Tap to review stock and generate supplier purchase order",
                                fontSize = 11.5.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Pending Due",
                    value = inrFormat.format(totalOutstanding),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = ErpRedDanger,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("invoices") }
                )
                StatCard(
                    title = "GST Output",
                    value = inrFormat.format(totalTaxCollected),
                    icon = Icons.Default.Percent,
                    color = ErpTealPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("reports") }
                )
            }
        }

        // Quick Action Grid
        item {
            Text(
                text = "Quick Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionItem(
                    label = "New Invoice",
                    icon = Icons.Default.Receipt,
                    bgColor = ErpTealPrimary,
                    tint = Color.White,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_new_invoice"),
                    onClick = { onNavigate("create_invoice") }
                )
                QuickActionItem(
                    label = "Inventory",
                    icon = Icons.Default.Inventory2,
                    bgColor = Color(0xFFEFF6FF),
                    tint = Color(0xFF2563EB),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_inventory"),
                    onClick = { onNavigate("inventory") }
                )
                QuickActionItem(
                    label = "Clients",
                    icon = Icons.Default.People,
                    bgColor = Color(0xFFFDF2F8),
                    tint = Color(0xFFDB2777),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_clients"),
                    onClick = { onNavigate("clients") }
                )
                QuickActionItem(
                    label = "Expenses",
                    icon = Icons.Default.MoneyOff,
                    bgColor = Color(0xFFFEF2F2),
                    tint = ErpRedDanger,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_expenses"),
                    onClick = { onNavigate("expenses") }
                )
            }
        }

        // Additional Tools Row (Catalog & CA Portal)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("catalog") }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = ErpTealPrimary)
                        Column {
                            Text("Catalog Studio", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Digital store & links", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("reports") }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = ErpAmberAccent)
                        Column {
                            Text("CA Tax Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("GSTR-1 & Tax Audit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Recent Invoices section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Invoices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { onNavigate("invoices") }) {
                    Text("View All", color = ErpTealPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(invoices.take(5)) { invoice ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectInvoice(invoice.id) }
                    .testTag("invoice_item_${invoice.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(ErpTealLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = ErpTealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = invoice.clientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${invoice.invoiceNumber} • ${dateFormat.format(Date(invoice.date))}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = inrFormat.format(invoice.grandTotal),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = ErpTealPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        PaymentStatusBadge(status = invoice.paymentStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun QuickActionItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
