package com.example

sealed interface Screen {
    data object Dashboard : Screen
    data object Invoices : Screen
    data object Inventory : Screen
    data object Clients : Screen
    data object Expenses : Screen
    data object TaxReports : Screen
    data object Catalog : Screen
    data object Settings : Screen
    data class InvoiceDetail(val invoiceId: String) : Screen
    data object CreateInvoice : Screen
}
