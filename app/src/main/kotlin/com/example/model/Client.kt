package com.example.model

data class Client(
    val id: String,
    val name: String,
    val businessName: String = "",
    val phone: String,
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "Haryana",
    val gstin: String = "",
    val openingBalance: Double = 0.0
)
