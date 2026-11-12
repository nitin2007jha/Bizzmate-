package com.example.model

data class Expense(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val date: Long,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val reference: String = "",
    val notes: String = ""
)
