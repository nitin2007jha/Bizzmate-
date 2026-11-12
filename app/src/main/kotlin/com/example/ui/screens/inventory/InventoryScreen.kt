package com.example.ui.screens.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.Product
import com.example.ui.components.StockBadge
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpRedDanger
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.util.*

@Composable
fun InventoryScreen(
    repository: ErpRepository,
    onOpenVoiceAssistant: () -> Unit
) {
    val products by repository.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productForStockAdjust by remember { mutableStateOf<Product?>(null) }

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val categories = remember(products) {
        products.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory, filterLowStockOnly) {
        products.filter { p ->
            val matchQuery = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == null || p.category == selectedCategory
            val matchLow = !filterLowStockOnly || p.isLowStock || p.isOutOfStock
            matchQuery && matchCat && matchLow
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddProductDialog = true
                },
                containerColor = ErpTealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products, SKU or category", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories & Low Stock Toggle
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = filterLowStockOnly,
                        onClick = { filterLowStockOnly = !filterLowStockOnly },
                        label = { Text("⚠️ Low Stock", fontSize = 12.sp) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All (${products.size})", fontSize = 12.sp) }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No products found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_item_${product.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        )
                                        Text(
                                            text = "${product.category} • SKU: ${product.sku.ifBlank { "N/A" }} • HSN: ${product.hsn}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    StockBadge(stock = product.stock, minAlert = product.minStockAlert)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = inrFormat.format(product.price),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = ErpTealPrimary
                                            )
                                            if (product.mrp > product.price) {
                                                Text(
                                                    text = inrFormat.format(product.mrp),
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Cost: ${inrFormat.format(product.purchasePrice)} • GST: ${product.gstRate.toInt()}%",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Quick Stock Adjust Buttons
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = { productForStockAdjust = product },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Stock ±", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                productToEdit = product
                                                showAddProductDialog = true
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(17.dp))
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

    // Add / Edit Product Dialog
    if (showAddProductDialog) {
        val editing = productToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var category by remember { mutableStateOf(editing?.category ?: "Ayurvedic Medicines") }
        var sku by remember { mutableStateOf(editing?.sku ?: "") }
        var hsn by remember { mutableStateOf(editing?.hsn ?: "30049011") }
        var price by remember { mutableStateOf(editing?.price?.toString() ?: "250") }
        var mrp by remember { mutableStateOf(editing?.mrp?.toString() ?: "299") }
        var purchasePrice by remember { mutableStateOf(editing?.purchasePrice?.toString() ?: "180") }
        var stock by remember { mutableStateOf(editing?.stock?.toString() ?: "20") }
        var unit by remember { mutableStateOf(editing?.unit ?: "Pcs") }
        var gstRate by remember { mutableStateOf(editing?.gstRate?.toString() ?: "12") }
        var description by remember { mutableStateOf(editing?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text(if (editing != null) "Edit Product" else "Add New Product", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit (Pcs/Kg)") },
                                modifier = Modifier.weight(0.9f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU / Code") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = hsn,
                                onValueChange = { hsn = it },
                                label = { Text("HSN Code") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Sale Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = mrp,
                                onValueChange = { mrp = it },
                                label = { Text("MRP (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = purchasePrice,
                                onValueChange = { purchasePrice = it },
                                label = { Text("Cost Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = { Text("Initial Stock") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (Optional)") },
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
                            val prod = Product(
                                id = editing?.id ?: "prod_${System.currentTimeMillis()}",
                                name = name.trim(),
                                category = category.trim().ifBlank { "General" },
                                sku = sku.trim(),
                                hsn = hsn.trim(),
                                price = price.toDoubleOrNull() ?: 100.0,
                                mrp = mrp.toDoubleOrNull() ?: (price.toDoubleOrNull() ?: 100.0),
                                purchasePrice = purchasePrice.toDoubleOrNull() ?: 70.0,
                                stock = stock.toIntOrNull() ?: 0,
                                unit = unit.trim().ifBlank { "Pcs" },
                                gstRate = gstRate.toDoubleOrNull() ?: 12.0,
                                description = description.trim()
                            )
                            if (editing != null) {
                                repository.updateProduct(prod)
                            } else {
                                repository.addProduct(prod)
                            }
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Quick Stock Adjust Dialog
    productForStockAdjust?.let { prod ->
        var adjustAmountText by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { productForStockAdjust = null },
            title = { Text("Adjust Stock: ${prod.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current Stock: ${prod.stock} ${prod.unit}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it },
                        label = { Text("Adjustment Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val amt = adjustAmountText.toIntOrNull() ?: 0
                            repository.adjustStock(prod.id, -amt)
                            productForStockAdjust = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErpRedDanger)
                    ) {
                        Text("- Deduct")
                    }

                    Button(
                        onClick = {
                            val amt = adjustAmountText.toIntOrNull() ?: 0
                            repository.adjustStock(prod.id, amt)
                            productForStockAdjust = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErpGreenSuccess)
                    ) {
                        Text("+ Add Stock")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { productForStockAdjust = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
