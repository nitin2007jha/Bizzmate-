package com.example.ui.screens.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ErpRepository
import com.example.model.*
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    repository: ErpRepository,
    onBack: () -> Unit,
    onInvoiceCreated: (String) -> Unit
) {
    val clients by repository.clients.collectAsState()
    val products by repository.products.collectAsState()
    val profile by repository.businessProfile.collectAsState()

    var selectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: "") }
    var selectedGstType by remember { mutableStateOf(GstType.INTRA_STATE) }
    var isInclusive by remember { mutableStateOf(profile.isGstInclusive) }
    var paymentStatus by remember { mutableStateOf(PaymentStatus.PAID) }
    var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }

    var discountAmountText by remember { mutableStateOf("0") }
    var shippingChargeText by remember { mutableStateOf("0") }
    var notesText by remember { mutableStateOf("") }

    // Line Items State
    val items = remember {
        mutableStateListOf<InvoiceItem>().apply {
            val firstProd = products.firstOrNull()
            if (firstProd != null) {
                add(
                    InvoiceItem(
                        id = UUID.randomUUID().toString(),
                        productId = firstProd.id,
                        name = firstProd.name,
                        hsn = firstProd.hsn,
                        quantity = 1.0,
                        unit = firstProd.unit,
                        rate = firstProd.price,
                        gstRate = firstProd.gstRate
                    )
                )
            }
        }
    }

    var showAddItemDialog by remember { mutableStateOf(false) }
    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Live Calculation
    val discountAmount = discountAmountText.toDoubleOrNull() ?: 0.0
    val shippingCharge = shippingChargeText.toDoubleOrNull() ?: 0.0
    val subtotal = items.sumOf { it.lineNet }
    val totalTaxable = items.sumOf { it.calculateTax(isInclusive).first }
    val totalTax = items.sumOf { it.calculateTax(isInclusive).second }
    val grandTotal = if (isInclusive) {
        (subtotal - discountAmount + shippingCharge).coerceAtLeast(0.0)
    } else {
        (totalTaxable + totalTax - discountAmount + shippingCharge).coerceAtLeast(0.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = inrFormat.format(grandTotal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ErpTealPrimary
                        )
                    }

                    Button(
                        onClick = {
                            val selectedClient = clients.find { it.id == selectedClientId }
                                ?: Client(id = "cli_walkin", name = "Retail Customer", phone = "")
                            val invId = "inv_${System.currentTimeMillis()}"
                            val invoice = Invoice(
                                id = invId,
                                invoiceNumber = repository.generateNextInvoiceNumber(),
                                date = System.currentTimeMillis(),
                                clientId = selectedClient.id,
                                clientName = selectedClient.name,
                                clientPhone = selectedClient.phone,
                                clientAddress = selectedClient.address,
                                clientGstin = selectedClient.gstin,
                                items = items.toList(),
                                discountAmount = discountAmount,
                                shippingCharge = shippingCharge,
                                gstType = selectedGstType,
                                isGstInclusive = isInclusive,
                                paymentStatus = paymentStatus,
                                paymentMode = paymentMode,
                                amountPaid = if (paymentStatus == PaymentStatus.PAID) grandTotal else 0.0,
                                notes = notesText
                            )
                            repository.addInvoice(invoice)
                            onInvoiceCreated(invId)
                        },
                        enabled = items.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_invoice_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Invoice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Customer Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Customer Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer Picker Dropdown / Radio selection
                        var clientDropdownExpanded by remember { mutableStateOf(false) }
                        val currentClient = clients.find { it.id == selectedClientId } ?: clients.firstOrNull()

                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { clientDropdownExpanded = true }
                                .testTag("select_client_card"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentClient?.name ?: "Select Customer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (currentClient != null && currentClient.phone.isNotBlank()) {
                                        Text(
                                            text = "Phone: ${currentClient.phone}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text("Change", color = ErpTealPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = clientDropdownExpanded,
                            onDismissRequest = { clientDropdownExpanded = false }
                        ) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(c.name, fontWeight = FontWeight.Bold)
                                            if (c.phone.isNotBlank()) Text(c.phone, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedClientId = c.id
                                        clientDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // GST Settings & Type
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tax & GST Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedGstType == GstType.INTRA_STATE,
                                onClick = { selectedGstType = GstType.INTRA_STATE },
                                label = { Text("Intra-State (CGST + SGST)", fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = selectedGstType == GstType.INTER_STATE,
                                onClick = { selectedGstType = GstType.INTER_STATE },
                                label = { Text("Inter-State (IGST)", fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Inclusive GST Pricing", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Entered item rate already has GST included", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = isInclusive,
                                onCheckedChange = { isInclusive = it }
                            )
                        }
                    }
                }
            }

            // Items Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Invoice Line Items (${items.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontWeight = FontWeight.Bold, color = ErpTealPrimary)
                    }
                }
            }

            itemsIndexed(items) { index, item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text(
                                "${item.quantity.toInt()} ${item.unit} × ${inrFormat.format(item.rate)} • ${item.gstRate.toInt()}% GST",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = inrFormat.format(item.lineNet),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { items.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Charges & Payment Options
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
                        Text("Discounts & Payment", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = discountAmountText,
                                onValueChange = { discountAmountText = it },
                                label = { Text("Discount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = shippingChargeText,
                                onValueChange = { shippingChargeText = it },
                                label = { Text("Shipping (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentStatus.values().forEach { st ->
                                FilterChip(
                                    selected = paymentStatus == st,
                                    onClick = { paymentStatus = st },
                                    label = { Text(st.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMode.values().forEach { md ->
                                FilterChip(
                                    selected = paymentMode == md,
                                    onClick = { paymentMode = md },
                                    label = { Text(md.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            label = { Text("Invoice Notes (Optional)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Bill Breakdown Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable Subtotal", fontSize = 12.5.sp)
                            Text(inrFormat.format(totalTaxable), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (selectedGstType == GstType.INTRA_STATE) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CGST", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(inrFormat.format(totalTax / 2.0), fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SGST", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(inrFormat.format(totalTax / 2.0), fontSize = 12.sp)
                            }
                        } else {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IGST", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(inrFormat.format(totalTax), fontSize = 12.sp)
                            }
                        }
                        if (discountAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount", fontSize = 12.5.sp, color = ErpGreenSuccess)
                                Text("-${inrFormat.format(discountAmount)}", fontSize = 12.5.sp, color = ErpGreenSuccess, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (shippingCharge > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery / Shipping", fontSize = 12.5.sp)
                                Text("+${inrFormat.format(shippingCharge)}", fontSize = 12.5.sp)
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Grand Total", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(inrFormat.format(grandTotal), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ErpTealPrimary)
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
        var quantityText by remember { mutableStateOf("1") }
        var customPriceText by remember { mutableStateOf(selectedProduct?.price?.toString() ?: "100") }

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Line Item", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Product from Inventory:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    var pDropdownExpanded by remember { mutableStateOf(false) }

                    OutlinedButton(
                        onClick = { pDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(selectedProduct?.name ?: "Choose Item", maxLines = 1)
                    }

                    DropdownMenu(
                        expanded = pDropdownExpanded,
                        onDismissRequest = { pDropdownExpanded = false }
                    ) {
                        products.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} - ${inrFormat.format(p.price)}") },
                                onClick = {
                                    selectedProduct = p
                                    customPriceText = p.price.toString()
                                    pDropdownExpanded = false
                                }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = customPriceText,
                            onValueChange = { customPriceText = it },
                            label = { Text("Rate (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 1.0
                        val rate = customPriceText.toDoubleOrNull() ?: (selectedProduct?.price ?: 100.0)
                        val p = selectedProduct
                        items.add(
                            InvoiceItem(
                                id = UUID.randomUUID().toString(),
                                productId = p?.id,
                                name = p?.name ?: "Custom Item",
                                hsn = p?.hsn ?: "30049011",
                                quantity = qty,
                                unit = p?.unit ?: "Pcs",
                                rate = rate,
                                gstRate = p?.gstRate ?: 12.0
                            )
                        )
                        showAddItemDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary)
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
