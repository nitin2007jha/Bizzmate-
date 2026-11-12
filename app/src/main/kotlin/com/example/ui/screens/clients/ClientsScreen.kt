package com.example.ui.screens.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.Client
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpRedDanger
import com.example.ui.theme.ErpTealLight
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.util.*

@Composable
fun ClientsScreen(
    repository: ErpRepository,
    onSelectClientInvoices: (String) -> Unit
) {
    val clients by repository.clients.collectAsState()
    val invoices by repository.invoices.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }

    val context = LocalContext.current
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val filteredClients = remember(clients, searchQuery) {
        clients.filter { c ->
            searchQuery.isBlank() ||
                c.name.contains(searchQuery, ignoreCase = true) ||
                c.phone.contains(searchQuery, ignoreCase = true) ||
                c.businessName.contains(searchQuery, ignoreCase = true) ||
                c.city.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clientToEdit = null
                    showAddDialog = true
                },
                containerColor = ErpTealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_client_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search clients by name, phone or city", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredClients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No customers found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredClients) { client ->
                        val clientInvoices = invoices.filter { it.clientId == client.id }
                        val pendingDue = clientInvoices.sumOf { it.balanceDue } + client.openingBalance

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("client_card_${client.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            color = ErpTealLight,
                                            shape = CircleShape,
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = client.name.take(1).uppercase(),
                                                    color = ErpTealPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = client.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp
                                            )
                                            if (client.businessName.isNotBlank()) {
                                                Text(
                                                    text = client.businessName,
                                                    fontSize = 11.5.sp,
                                                    color = ErpTealPrimary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            if (client.city.isNotBlank()) {
                                                Text(
                                                    text = "${client.city}, ${client.state}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (pendingDue > 0) {
                                            Text("Pending Due", fontSize = 10.5.sp, color = ErpRedDanger)
                                            Text(
                                                text = inrFormat.format(pendingDue),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = ErpRedDanger
                                            )
                                        } else {
                                            Text("All Clear", fontSize = 11.sp, color = ErpGreenSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${clientInvoices.size} Invoices",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (client.phone.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = "Call", tint = ErpTealPrimary, modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    val cleanPhone = client.phone.replace(Regex("[^0-9]"), "")
                                                    val waPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waPhone")
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.Message, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                clientToEdit = client
                                                showAddDialog = true
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val editing = clientToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var businessName by remember { mutableStateOf(editing?.businessName ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var email by remember { mutableStateOf(editing?.email ?: "") }
        var address by remember { mutableStateOf(editing?.address ?: "") }
        var city by remember { mutableStateOf(editing?.city ?: "") }
        var gstin by remember { mutableStateOf(editing?.gstin ?: "") }
        var openingBalanceText by remember { mutableStateOf(editing?.openingBalance?.toString() ?: "0") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editing != null) "Edit Customer" else "Add New Customer", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Customer Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Store Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Phone *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = { Text("GSTIN (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = openingBalanceText,
                                onValueChange = { openingBalanceText = it },
                                label = { Text("Opening Bal (₹)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Billing Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val client = Client(
                                id = editing?.id ?: "cli_${System.currentTimeMillis()}",
                                name = name.trim(),
                                businessName = businessName.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                gstin = gstin.trim(),
                                openingBalance = openingBalanceText.toDoubleOrNull() ?: 0.0
                            )
                            if (editing != null) {
                                repository.updateClient(client)
                            } else {
                                repository.addClient(client)
                            }
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
