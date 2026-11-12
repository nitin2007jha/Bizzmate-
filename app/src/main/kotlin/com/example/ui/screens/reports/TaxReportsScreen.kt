package com.example.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Share
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
import com.example.ui.theme.ErpAmberAccent
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.util.*

@Composable
fun TaxReportsScreen(
    repository: ErpRepository
) {
    val invoices by repository.invoices.collectAsState()
    val profile by repository.businessProfile.collectAsState()
    val context = LocalContext.current

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Computations
    val totalInvoicesCount = invoices.size
    val totalTurnover = invoices.sumOf { it.grandTotal }
    val totalTaxable = invoices.sumOf { it.totalTaxable }
    val totalTax = invoices.sumOf { it.totalTax }
    val cgst = invoices.sumOf { it.cgst }
    val sgst = invoices.sumOf { it.sgst }
    val igst = invoices.sumOf { it.igst }

    // Slabs breakdown (5%, 12%, 18%, 28%)
    val allItems = invoices.flatMap { it.items }
    val slabs = listOf(5.0, 12.0, 18.0, 28.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ErpTealPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CA TAX & GST PORTAL", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("GSTR-1 Ready Summary", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            if (profile.gstin.isNotBlank()) {
                                Text("GSTIN: ${profile.gstin}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.5.sp)
                            }
                        }

                        IconButton(
                            onClick = {
                                val reportText = buildString {
                                    appendLine("📊 GST TAX SUMMARY - ${profile.name}")
                                    appendLine("GSTIN: ${profile.gstin}")
                                    appendLine("Total Invoices: $totalInvoicesCount")
                                    appendLine("Gross Turnover: ${inrFormat.format(totalTurnover)}")
                                    appendLine("Taxable Amount: ${inrFormat.format(totalTaxable)}")
                                    appendLine("Total Output GST: ${inrFormat.format(totalTax)}")
                                    appendLine("-----------------------------")
                                    appendLine("• CGST: ${inrFormat.format(cgst)}")
                                    appendLine("• SGST: ${inrFormat.format(sgst)}")
                                    appendLine("• IGST: ${inrFormat.format(igst)}")
                                    appendLine("-----------------------------")
                                    appendLine("Slab Breakdown:")
                                    slabs.forEach { slab ->
                                        val slabItems = allItems.filter { it.gstRate == slab }
                                        val slabTax = slabItems.sumOf { it.calculateTax(profile.isGstInclusive).second }
                                        appendLine("• ${slab.toInt()}% GST Tax: ${inrFormat.format(slabTax)}")
                                    }
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, reportText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Send GST Report to CA"))
                            },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                            modifier = Modifier.testTag("share_ca_report_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share to CA")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TOTAL OUTPUT TAX", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(inrFormat.format(totalTax), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TAXABLE TURNOVER", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(inrFormat.format(totalTaxable), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Tax Split Card (CGST, SGST, IGST)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GST Component Split", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaxComponentBox(label = "CGST", amount = inrFormat.format(cgst), modifier = Modifier.weight(1f))
                        TaxComponentBox(label = "SGST", amount = inrFormat.format(sgst), modifier = Modifier.weight(1f))
                        TaxComponentBox(label = "IGST", amount = inrFormat.format(igst), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Slab Table Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Tax Slab Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GST SLAB", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("ITEMS SOLD", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("TAX COLLECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }

                    slabs.forEach { slab ->
                        val slabItems = allItems.filter { it.gstRate == slab }
                        val slabCount = slabItems.sumOf { it.quantity }
                        val slabTax = slabItems.sumOf { it.calculateTax(profile.isGstInclusive).second }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${slab.toInt()}% GST", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("${slabCount.toInt()} units", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            Text(inrFormat.format(slabTax), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErpTealPrimary, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }
            }
        }

        // Export Actions
        item {
            Button(
                onClick = {
                    val reportText = buildString {
                        appendLine("CA AUDIT REPORT - ${profile.name}")
                        appendLine("Turnover: ${inrFormat.format(totalTurnover)}")
                        appendLine("Taxable: ${inrFormat.format(totalTaxable)}")
                        appendLine("Total GST: ${inrFormat.format(totalTax)}")
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share with Chartered Accountant"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_ca_btn")
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export & Share Report with CA")
            }
        }
    }
}

@Composable
fun TaxComponentBox(label: String, amount: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = ErpTealPrimary)
        }
    }
}
