package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentStatus
import com.example.ui.theme.ErpGreenSuccess
import com.example.ui.theme.ErpRedDanger

@Composable
fun PaymentStatusBadge(status: PaymentStatus, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status) {
        PaymentStatus.PAID -> Color(0xFFDCFCE7) to ErpGreenSuccess
        PaymentStatus.UNPAID -> Color(0xFFFEE2E2) to ErpRedDanger
        PaymentStatus.PARTIAL -> Color(0xFFFEF3C7) to Color(0xFFD97706)
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StockBadge(stock: Int, minAlert: Int, modifier: Modifier = Modifier) {
    val (text, bg, fg) = when {
        stock <= 0 -> Triple("Out of Stock", Color(0xFFFEE2E2), ErpRedDanger)
        stock <= minAlert -> Triple("Low: $stock left", Color(0xFFFEF3C7), Color(0xFFD97706))
        else -> Triple("In Stock ($stock)", Color(0xFFDCFCE7), ErpGreenSuccess)
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(99.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
