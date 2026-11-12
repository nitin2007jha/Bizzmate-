package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ErpRepository {
    private val _businessProfile = MutableStateFlow(SampleData.initialProfile)
    val businessProfile: StateFlow<BusinessProfile> = _businessProfile.asStateFlow()

    private val _products = MutableStateFlow(SampleData.initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _clients = MutableStateFlow(SampleData.initialClients)
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _invoices = MutableStateFlow(SampleData.createInitialInvoices())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _expenses = MutableStateFlow(SampleData.initialExpenses)
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    fun updateBusinessProfile(profile: BusinessProfile) {
        _businessProfile.value = profile
    }

    // Product actions
    fun addProduct(product: Product) {
        _products.update { listOf(product) + it }
    }

    fun updateProduct(product: Product) {
        _products.update { list -> list.map { if (it.id == product.id) product else it } }
    }

    fun deleteProduct(productId: String) {
        _products.update { list -> list.filterNot { it.id == productId } }
    }

    fun adjustStock(productId: String, delta: Int) {
        _products.update { list ->
            list.map {
                if (it.id == productId) {
                    it.copy(stock = (it.stock + delta).coerceAtLeast(0))
                } else it
            }
        }
    }

    // Client actions
    fun addClient(client: Client) {
        _clients.update { listOf(client) + it }
    }

    fun updateClient(client: Client) {
        _clients.update { list -> list.map { if (it.id == client.id) client else it } }
    }

    fun deleteClient(clientId: String) {
        _clients.update { list -> list.filterNot { it.id == clientId } }
    }

    // Invoice actions
    fun addInvoice(invoice: Invoice) {
        _invoices.update { listOf(invoice) + it }
        // Decrement stock for inventory items sold
        invoice.items.forEach { item ->
            if (item.productId != null) {
                adjustStock(item.productId, -item.quantity.toInt())
            }
        }
    }

    fun updateInvoice(invoice: Invoice) {
        _invoices.update { list -> list.map { if (it.id == invoice.id) invoice else it } }
    }

    fun deleteInvoice(invoiceId: String) {
        _invoices.update { list -> list.filterNot { it.id == invoiceId } }
    }

    fun markInvoicePaid(invoiceId: String, mode: PaymentMode = PaymentMode.UPI) {
        _invoices.update { list ->
            list.map {
                if (it.id == invoiceId) {
                    it.copy(
                        paymentStatus = PaymentStatus.PAID,
                        amountPaid = it.grandTotal,
                        paymentMode = mode
                    )
                } else it
            }
        }
    }

    // Expense actions
    fun addExpense(expense: Expense) {
        _expenses.update { listOf(expense) + it }
    }

    fun deleteExpense(expenseId: String) {
        _expenses.update { list -> list.filterNot { it.id == expenseId } }
    }

    fun generateNextInvoiceNumber(): String {
        val count = _invoices.value.size + 1
        return "INV-2026-%03d".format(count)
    }

    // Voice query interpretation matching voice-erp.js
    fun processVoiceQuery(query: String): VoiceQueryResult {
        val q = query.lowercase().trim()
        val inrFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // Check for today's sales
        if (q.contains("today sales") || q.contains("aaj") || q.contains("आज") ||
            q.contains("collection") || q.contains("कलेक्शन") || q.contains("revenue") || q.contains("कमाई")
        ) {
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val todayInvoices = _invoices.value.filter { it.date >= startOfToday }
            val totalCollection = todayInvoices.sumOf { it.amountPaid }
            val cash = todayInvoices.filter { it.paymentMode == PaymentMode.CASH }.sumOf { it.amountPaid }
            val upi = todayInvoices.filter { it.paymentMode == PaymentMode.UPI }.sumOf { it.amountPaid }

            val text = "आज का कुल कलेक्शन ${inrFormat.format(totalCollection)} है (${todayInvoices.size} इनवॉइस). नकद: ${inrFormat.format(cash)}, यूपीआई: ${inrFormat.format(upi)}."
            return VoiceQueryResult(
                title = "Today's Collection",
                response = text,
                targetScreen = "dashboard",
                dataPoints = listOf(
                    "Total Collection" to inrFormat.format(totalCollection),
                    "Cash" to inrFormat.format(cash),
                    "UPI" to inrFormat.format(upi),
                    "Bills Count" to "${todayInvoices.size}"
                )
            )
        }

        // Check for low stock
        if (q.contains("low stock") || q.contains("कम स्टॉक") || q.contains("stock alert") ||
            q.contains("reorder") || q.contains("स्टॉक") || q.contains("खत्म")
        ) {
            val lowItems = _products.value.filter { it.isLowStock || it.isOutOfStock }
            val text = if (lowItems.isEmpty()) {
                "सभी प्रोडक्ट्स का स्टॉक पर्याप्त है। कोई आइटम कम नहीं है।"
            } else {
                "कुल ${lowItems.size} प्रोडक्ट्स में कम स्टॉक है: " + lowItems.take(3).joinToString { "${it.name} (${it.stock} बचे)" }
            }
            return VoiceQueryResult(
                title = "Stock Alert / कम स्टॉक",
                response = text,
                targetScreen = "inventory",
                dataPoints = lowItems.take(5).map { it.name to "${it.stock} ${it.unit}" }
            )
        }

        // Check for GST / Tax
        if (q.contains("gst") || q.contains("tax") || q.contains("जीएसटी") || q.contains("टैक्स")) {
            val totalTax = _invoices.value.sumOf { it.totalTax }
            val totalTaxable = _invoices.value.sumOf { it.totalTaxable }
            val cgst = _invoices.value.sumOf { it.cgst }
            val sgst = _invoices.value.sumOf { it.sgst }
            val igst = _invoices.value.sumOf { it.igst }

            val text = "कुल जीएसटी टैक्स संकलन ${inrFormat.format(totalTax)} है (कर योग्य बिक्री: ${inrFormat.format(totalTaxable)}). CGST: ${inrFormat.format(cgst)}, SGST: ${inrFormat.format(sgst)}, IGST: ${inrFormat.format(igst)}."
            return VoiceQueryResult(
                title = "GST Summary / जीएसटी रिपोर्ट",
                response = text,
                targetScreen = "reports",
                dataPoints = listOf(
                    "Total Tax" to inrFormat.format(totalTax),
                    "Taxable Turnover" to inrFormat.format(totalTaxable),
                    "CGST + SGST" to inrFormat.format(cgst + sgst),
                    "IGST" to inrFormat.format(igst)
                )
            )
        }

        // Check for navigation
        if (q.contains("invoice") || q.contains("बिल") || q.contains("bill") || q.contains("इनवॉइस")) {
            return VoiceQueryResult(
                title = "New Invoice / नया बिल",
                response = "इनवॉइस स्क्रीन खोल रहा हूँ। आप नया बिल बना सकते हैं।",
                targetScreen = "invoices",
                dataPoints = listOf("Action" to "Open Invoice Manager")
            )
        }

        if (q.contains("client") || q.contains("customer") || q.contains("ग्राहक") || q.contains("क्लाइंट")) {
            val totalOutstanding = _clients.value.sumOf { it.openingBalance }
            return VoiceQueryResult(
                title = "Clients Directory",
                response = "क्लाइंट लिस्ट खोली गई। कुल ${_clients.value.size} ग्राहक हैं। कुल बकाया: ${inrFormat.format(totalOutstanding)}",
                targetScreen = "clients",
                dataPoints = listOf("Total Clients" to "${_clients.value.size}")
            )
        }

        if (q.contains("expense") || q.contains("खर्च") || q.contains("खर्चा")) {
            val totalExpenses = _expenses.value.sumOf { it.amount }
            return VoiceQueryResult(
                title = "Expenses / खर्चे",
                response = "कुल दर्ज खर्चे: ${inrFormat.format(totalExpenses)}",
                targetScreen = "expenses",
                dataPoints = listOf("Total Expenses" to inrFormat.format(totalExpenses))
            )
        }

        // Default fallback
        return VoiceQueryResult(
            title = "Voice Assistant / आवाज़ सहायक",
            response = "मैंने सुना: \"$query\"। आप बोल सकते हैं: 'आज की सेल', 'कम स्टॉक', 'जीएसटी रिपोर्ट', 'नया बिल' या 'खर्चे'।",
            targetScreen = "dashboard",
            dataPoints = listOf(
                "Try saying" to "आज की सेल (Today's sales)",
                "Try saying" to "कम स्टॉक (Low stock items)",
                "Try saying" to "जीएसटी रिपोर्ट (GST tax summary)"
            )
        )
    }

    companion object {
        val instance by lazy { ErpRepository() }
    }
}

data class VoiceQueryResult(
    val title: String,
    val response: String,
    val targetScreen: String,
    val dataPoints: List<Pair<String, String>> = emptyList()
)
