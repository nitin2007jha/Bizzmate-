package com.example.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val sku: String = "",
    val hsn: String = "30049011",
    val price: Double,
    val mrp: Double = price,
    val purchasePrice: Double = price * 0.7,
    val stock: Int = 0,
    val minStockAlert: Int = 10,
    val unit: String = "Pcs",
    val gstRate: Double = 12.0,
    val description: String = ""
) {
    val isLowStock: Boolean
        get() = stock in 1..minStockAlert

    val isOutOfStock: Boolean
        get() = stock <= 0
}
