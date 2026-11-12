package com.example.ui.screens.catalog

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
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
import com.example.model.Product
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpTealPrimary
import java.text.NumberFormat
import java.util.*

@Composable
fun CatalogScreen(
    repository: ErpRepository
) {
    val products by repository.products.collectAsState()
    val profile by repository.businessProfile.collectAsState()
    val context = LocalContext.current

    val inrFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Catalog Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = ErpTealPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CATALOG STUDIO", color = Color.White.copy(alpha = 0.85f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        Text(profile.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Instant WhatsApp ordering catalog", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            val catalogText = buildString {
                                appendLine("🛍️ ${profile.name} - Product Catalog")
                                appendLine("${profile.tagline}")
                                appendLine("-----------------------------")
                                products.forEach { p ->
                                    val off = if (p.mrp > p.price) " (${((p.mrp - p.price) / p.mrp * 100).toInt()}% OFF)" else ""
                                    appendLine("• ${p.name}")
                                    appendLine("  Price: ${inrFormat.format(p.price)}$off | ${p.unit}")
                                }
                                appendLine("-----------------------------")
                                appendLine("To order, reply with item name & quantity.")
                                appendLine("WhatsApp/Call: ${profile.phone}")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, catalogText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Catalog"))
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White,
                            contentColor = ErpTealPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_catalog_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Catalog Items (${products.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products) { p ->
                val discountPercent = if (p.mrp > p.price) {
                    ((p.mrp - p.price) / p.mrp * 100).toInt()
                } else 0

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = p.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (discountPercent > 0) {
                                Surface(
                                    color = ErpGreenSuccess,
                                    shape = RoundedCornerShape(99.dp)
                                ) {
                                    Text(
                                        text = "$discountPercent% off",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = p.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = inrFormat.format(p.price),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                color = ErpTealPrimary
                            )
                            if (p.mrp > p.price) {
                                Text(
                                    text = inrFormat.format(p.mrp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(p.unit, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val cleanPhone = profile.phone.replace(Regex("[^0-9]"), "")
                                val waPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                val msg = "Hi, I want to order '${p.name}' priced at ${inrFormat.format(p.price)} from your catalog."
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waPhone&text=${Uri.encode(msg)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErpTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Order via WA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
