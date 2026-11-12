package com.example.model

enum class PaymentStatus(val label: String) {
    PAID("Paid"),
    UNPAID("Unpaid"),
    PARTIAL("Partial")
}

enum class PaymentMode(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    BANK_TRANSFER("Bank Transfer"),
    CHEQUE("Cheque")
}

enum class GstType(val label: String) {
    INTRA_STATE("Intra-State (CGST + SGST)"),
    INTER_STATE("Inter-State (IGST)")
}

data class InvoiceItem(
    val id: String,
    val productId: String? = null,
    val name: String,
    val hsn: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val rate: Double = 0.0,
    val gstRate: Double = 12.0,
    val discountPercent: Double = 0.0
) {
    val lineGross: Double
        get() = quantity * rate

    val lineDiscount: Double
        get() = lineGross * (discountPercent / 100.0)

    val lineNet: Double
        get() = lineGross - lineDiscount

    fun calculateTax(isInclusive: Boolean): Pair<Double, Double> {
        return if (isInclusive) {
            val taxable = if (gstRate > 0) lineNet / (1.0 + (gstRate / 100.0)) else lineNet
            val tax = lineNet - taxable
            Pair(taxable, tax)
        } else {
            val taxable = lineNet
            val tax = if (gstRate > 0) taxable * (gstRate / 100.0) else 0.0
            Pair(taxable, tax)
        }
    }
}

data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val date: Long,
    val dueDate: Long = date + (7 * 24 * 60 * 60 * 1000L),
    val clientId: String,
    val clientName: String,
    val clientPhone: String = "",
    val clientAddress: String = "",
    val clientGstin: String = "",
    val items: List<InvoiceItem>,
    val discountAmount: Double = 0.0,
    val shippingCharge: Double = 0.0,
    val gstType: GstType = GstType.INTRA_STATE,
    val isGstInclusive: Boolean = true,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val paymentMode: PaymentMode = PaymentMode.UPI,
    val amountPaid: Double = 0.0,
    val notes: String = ""
) {
    val totalTaxable: Double
        get() = items.sumOf { it.calculateTax(isGstInclusive).first }

    val totalTax: Double
        get() = items.sumOf { it.calculateTax(isGstInclusive).second }

    val subtotal: Double
        get() = items.sumOf { it.lineNet }

    val grandTotal: Double
        get() {
            return if (isGstInclusive) {
                (subtotal - discountAmount + shippingCharge).coerceAtLeast(0.0)
            } else {
                (totalTaxable + totalTax - discountAmount + shippingCharge).coerceAtLeast(0.0)
            }
        }

    val balanceDue: Double
        get() = (grandTotal - amountPaid).coerceAtLeast(0.0)

    val cgst: Double
        get() = if (gstType == GstType.INTRA_STATE) totalTax / 2.0 else 0.0

    val sgst: Double
        get() = if (gstType == GstType.INTRA_STATE) totalTax / 2.0 else 0.0

    val igst: Double
        get() = if (gstType == GstType.INTER_STATE) totalTax else 0.0
}
