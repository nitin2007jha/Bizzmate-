package com.example.ui.screens.invoices

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ErpRepository
import com.example.model.GstType
import com.example.model.PaymentMode
import com.example.model.PaymentStatus
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: String,
    repository: ErpRepository,
    onBack: () -> Unit
) {
    val invoices by repository.invoices.collectAsState()
    val profile by repository.businessProfile.collectAsState()
    val invoice = invoices.find { it.id == invoiceId }
    val context = LocalContext.current

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Invoice not found")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                appendLine("📄 TAX INVOICE - ${profile.name}")
                                appendLine("Invoice No: ${invoice.invoiceNumber}")
                                appendLine("Date: ${dateFormat.format(Date(invoice.date))}")
                                appendLine("Customer: ${invoice.clientName}")
                                appendLine("-----------------------------")
                                invoice.items.forEach { item ->
                                    appendLine("• ${item.name} (${item.quantity.toInt()} ${item.unit}) : ${inrFormat.format(item.lineNet)}")
                                }
                                appendLine("-----------------------------")
                                appendLine("Taxable Amount: ${inrFormat.format(invoice.totalTaxable)}")
                                appendLine("GST: ${inrFormat.format(invoice.totalTax)}")
                                appendLine("Grand Total: ${inrFormat.format(invoice.grandTotal)}")
                                appendLine("Status: ${invoice.paymentStatus.label}")
                                if (profile.upiId.isNotBlank()) {
                                    appendLine("Pay via UPI: ${profile.upiId}")
                                }
                                appendLine("Thank you for your business!")
                            }

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Invoice"))
                        },
                        modifier = Modifier.testTag("share_invoice_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (invoice.paymentStatus != PaymentStatus.PAID) {
                        Button(
                            onClick = {
                                repository.markInvoicePaid(invoice.id, PaymentMode.UPI)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErpGreenSuccess),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_paid_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark as Paid")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val cleanPhone = invoice.clientPhone.replace(Regex("[^0-9]"), "")
                            val waPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                            val text = "Namaste ${invoice.clientName}, your invoice ${invoice.invoiceNumber} for amount ${inrFormat.format(invoice.grandTotal)} is generated by ${profile.name}. Thank you!"
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waPhone&text=${Uri.encode(text)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("whatsapp_invoice_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send WhatsApp")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Document Card (Simulated Clean GST Invoice Layout)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Seller & Document Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text(profile.address, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${profile.city} - ${profile.pincode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (profile.gstin.isNotBlank()) {
                                    Text("GSTIN: ${profile.gstin}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary)
                                }
                                Text("Phone: ${profile.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = ErpTealPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "TAX INVOICE",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = ErpTealPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Date: ${dateFormat.format(Date(invoice.date))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Buyer Info
                        Text("BILLED TO:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(invoice.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (invoice.clientAddress.isNotBlank()) {
                            Text(invoice.clientAddress, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (invoice.clientPhone.isNotBlank()) {
                            Text("Ph: ${invoice.clientPhone}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (invoice.clientGstin.isNotBlank()) {
                            Text("Customer GSTIN: ${invoice.clientGstin}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ErpTealPrimary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ITEM / DESCRIPTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary, modifier = Modifier.weight(2f))
                            Text("QTY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary, modifier = Modifier.weight(0.7f))
                            Text("RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary, modifier = Modifier.weight(1f))
                            Text("AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Line Items
                        invoice.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (item.hsn.isNotBlank()) {
                                        Text("HSN: ${item.hsn} (${item.gstRate.toInt()}% GST)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text("${item.quantity.toInt()} ${item.unit}", fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                                Text(inrFormat.format(item.rate), fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(inrFormat.format(item.lineNet), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Totals section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Taxable Amount:", fontSize = 12.sp)
                                Text(inrFormat.format(invoice.totalTaxable), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (invoice.gstType == GstType.INTRA_STATE) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CGST:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(inrFormat.format(invoice.cgst), fontSize = 11.5.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("SGST:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(inrFormat.format(invoice.sgst), fontSize = 11.5.sp)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("IGST:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(inrFormat.format(invoice.igst), fontSize = 11.5.sp)
                                }
                            }

                            if (invoice.discountAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount:", fontSize = 12.sp, color = ErpGreenSuccess)
                                    Text("-${inrFormat.format(invoice.discountAmount)}", fontSize = 12.sp, color = ErpGreenSuccess)
                                }
                            }

                            if (invoice.shippingCharge > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Shipping:", fontSize = 12.sp)
                                    Text("+${inrFormat.format(invoice.shippingCharge)}", fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Grand Total:", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                Text(inrFormat.format(invoice.grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = ErpTealPrimary)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Status:", fontSize = 12.sp)
                                PaymentStatusBadge(status = invoice.paymentStatus)
                            }
                        }

                        // Bank / UPI details
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("PAYMENT INSTRUCTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary)
                                if (profile.upiId.isNotBlank()) {
                                    Text("UPI ID: ${profile.upiId}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                if (profile.bankName.isNotBlank()) {
                                    Text("Bank: ${profile.bankName} • A/C: ${profile.accountNumber} • IFSC: ${profile.ifscCode}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (profile.invoiceTerms.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Terms & Conditions:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(profile.invoiceTerms, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Invoice?") },
            text = { Text("Are you sure you want to delete ${invoice.invoiceNumber}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteInvoice(invoice.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
