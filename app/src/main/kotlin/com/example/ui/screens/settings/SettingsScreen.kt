package com.example.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
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
import com.example.model.BusinessProfile
import com.example.ui.theme.ErpTealPrimary
import java.util.Date

@Composable
fun SettingsScreen(
    repository: ErpRepository
) {
    val profile by repository.businessProfile.collectAsState()
    val invoices by repository.invoices.collectAsState()
    val products by repository.products.collectAsState()
    val clients by repository.clients.collectAsState()
    val expenses by repository.expenses.collectAsState()

    var name by remember(profile) { mutableStateOf(profile.name) }
    var tagline by remember(profile) { mutableStateOf(profile.tagline) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var address by remember(profile) { mutableStateOf(profile.address) }
    var gstin by remember(profile) { mutableStateOf(profile.gstin) }
    var upiId by remember(profile) { mutableStateOf(profile.upiId) }
    var bankName by remember(profile) { mutableStateOf(profile.bankName) }
    var accountNum by remember(profile) { mutableStateOf(profile.accountNumber) }
    var ifsc by remember(profile) { mutableStateOf(profile.ifscCode) }
    var terms by remember(profile) { mutableStateOf(profile.invoiceTerms) }

    var saveToastVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Business Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Customize your company details for invoices and receipts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Company Information Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Company Identity", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Business / Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Sub-heading") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = gstin,
                        onValueChange = { gstin = it },
                        label = { Text("GSTIN Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Store / Office Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Payment & Bank Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Payment & Banking Info", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID (e.g. name@okaxis)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = accountNum,
                            onValueChange = { accountNum = it },
                            label = { Text("Account No.") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = ifsc,
                            onValueChange = { ifsc = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = terms,
                        onValueChange = { terms = it },
                        label = { Text("Default Invoice Terms") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    repository.updateBusinessProfile(
                        profile.copy(
                            name = name.trim(),
                            tagline = tagline.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            gstin = gstin.trim(),
                            upiId = upiId.trim(),
                            bankName = bankName.trim(),
                            accountNumber = accountNum.trim(),
                            ifscCode = ifsc.trim(),
                            invoiceTerms = terms.trim()
                        )
                    )
                    saveToastVisible = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile Settings")
            }
        }

        // Data Export Card (Ported from tools/data-export/)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Data Backup & Export", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "Export complete business records (${invoices.size} invoices, ${products.size} products, ${clients.size} customers, ${expenses.size} expenses) to share or backup.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            val backupText = buildString {
                                appendLine("=== MY BUSINESS ERP EXPORT ===")
                                appendLine("Business: ${profile.name} (GSTIN: ${profile.gstin})")
                                appendLine("Date: ${Date()}")
                                appendLine("Total Invoices: ${invoices.size}")
                                appendLine("Total Inventory Items: ${products.size}")
                                appendLine("Total Clients: ${clients.size}")
                                appendLine("Total Expenses: ${expenses.size}")
                                appendLine("-----------------------------")
                                appendLine("INVOICES:")
                                invoices.forEach {
                                    appendLine("${it.invoiceNumber} | ${it.clientName} | ₹${it.grandTotal} | ${it.paymentStatus.label}")
                                }
                                appendLine("-----------------------------")
                                appendLine("INVENTORY:")
                                products.forEach {
                                    appendLine("${it.name} | SKU: ${it.sku} | Stock: ${it.stock} ${it.unit} | ₹${it.price}")
                                }
                            }

                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, backupText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "Export ERP Data"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export All Records")
                    }
                }
            }
        }
    }

    if (saveToastVisible) {
        AlertDialog(
            onDismissRequest = { saveToastVisible = false },
            title = { Text("Settings Saved") },
            text = { Text("Your business details have been updated and will appear on newly generated invoices.") },
            confirmButton = {
                Button(onClick = { saveToastVisible = false }) {
                    Text("OK")
                }
            }
        )
    }
}
