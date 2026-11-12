package com.example.data

import com.example.model.*
import java.util.Calendar

object SampleData {
    val initialProfile = BusinessProfile(
        name = "Dev Ayurveda & Herbals",
        tagline = "Authentic Ayurvedic Medicines & Wellness Formulations",
        phone = "+91 98123 45678",
        email = "orders@devayurveda.in",
        address = "SCO 14-15, Main Market, Sector 14",
        city = "Gurugram, Haryana",
        pincode = "122001",
        gstin = "06AABBD1234F1Z9",
        upiId = "devayurveda@okaxis",
        isGstInclusive = true,
        bankName = "HDFC Bank Ltd",
        accountNumber = "50200038491823",
        ifscCode = "HDFC0000240",
        invoiceTerms = "1. Goods once sold will not be taken back or exchanged.\n2. Payment terms: Net 7 days from invoice date.\n3. All disputes subject to Gurugram jurisdiction only."
    )

    val initialProducts = listOf(
        Product(
            id = "prod_1",
            name = "Chyawanprash Awaleha (Special)",
            category = "Immunity",
            sku = "CHY-500",
            hsn = "30049011",
            price = 395.0,
            mrp = 450.0,
            purchasePrice = 270.0,
            stock = 45,
            minStockAlert = 15,
            unit = "Jar (500g)",
            gstRate = 12.0,
            description = "Traditional 51-herb Ayurvedic formulation with fresh Amla and pure honey."
        ),
        Product(
            id = "prod_2",
            name = "Ashwagandha Gold Capsules",
            category = "Stress & Vitality",
            sku = "ASH-60C",
            hsn = "30049011",
            price = 540.0,
            mrp = 650.0,
            purchasePrice = 360.0,
            stock = 18,
            minStockAlert = 10,
            unit = "Bottle (60 Cap)",
            gstRate = 12.0,
            description = "Standardized 5% Withanolides extract for endurance and mental calm."
        ),
        Product(
            id = "prod_3",
            name = "Triphala Churna Pure",
            category = "Digestion",
            sku = "TRI-200",
            hsn = "30049011",
            price = 145.0,
            mrp = 175.0,
            purchasePrice = 90.0,
            stock = 60,
            minStockAlert = 20,
            unit = "Pouch (200g)",
            gstRate = 5.0,
            description = "Equal blend of Haritaki, Bibhitaki, and Amalaki for gut balance."
        ),
        Product(
            id = "prod_4",
            name = "Mahanarayan Taila (Joint Relief)",
            category = "Pain Relief Oils",
            sku = "MAH-100",
            hsn = "30049011",
            price = 280.0,
            mrp = 325.0,
            purchasePrice = 185.0,
            stock = 8, // Low stock!
            minStockAlert = 12,
            unit = "Bottle (100ml)",
            gstRate = 12.0,
            description = "Medicated sesame oil for arthritis, knee pain, and stiffness."
        ),
        Product(
            id = "prod_5",
            name = "Brahmi Vati with Gold",
            category = "Brain & Memory",
            sku = "BRH-30T",
            hsn = "30049011",
            price = 680.0,
            mrp = 799.0,
            purchasePrice = 470.0,
            stock = 4, // Very low stock!
            minStockAlert = 10,
            unit = "Strip (30 Tab)",
            gstRate = 12.0,
            description = "Formulation for focus, memory retention, and sleeplessness."
        ),
        Product(
            id = "prod_6",
            name = "Kumkumadi Face Glow Tailam",
            category = "Skin Care",
            sku = "KUM-30M",
            hsn = "33049910",
            price = 850.0,
            mrp = 999.0,
            purchasePrice = 520.0,
            stock = 24,
            minStockAlert = 8,
            unit = "Dropper (30ml)",
            gstRate = 18.0,
            description = "Pure Kashmiri saffron facial beauty nectar for hyperpigmentation."
        ),
        Product(
            id = "prod_7",
            name = "Khadirarishta Blood Purifier",
            category = "Asava & Arishta",
            sku = "KHA-450",
            hsn = "30049011",
            price = 195.0,
            mrp = 220.0,
            purchasePrice = 130.0,
            stock = 0, // Out of stock!
            minStockAlert = 10,
            unit = "Bottle (450ml)",
            gstRate = 12.0,
            description = "Naturally fermented herbal tonic for glowing clear skin."
        )
    )

    val initialClients = listOf(
        Client(
            id = "cli_1",
            name = "Dr. Rajesh Sharma",
            businessName = "Sharma Ayurvedic Clinic",
            phone = "9823456781",
            email = "drsharma@ayurclinic.com",
            address = "Shop 5, Guru Nanak Market, Civil Lines",
            city = "Gurugram",
            state = "Haryana",
            gstin = "06ABFPS8291P1ZK",
            openingBalance = 1200.0
        ),
        Client(
            id = "cli_2",
            name = "Sunita Verma",
            businessName = "Prakriti Health Store",
            phone = "9871122334",
            email = "prakritistore@gmail.com",
            address = "B-12, Galleria Market, DLF Phase 4",
            city = "Gurugram",
            state = "Haryana",
            gstin = "06BCVPS1928K1ZL",
            openingBalance = 0.0
        ),
        Client(
            id = "cli_3",
            name = "Vikas Malhotra",
            businessName = "Malhotra Medicos",
            phone = "9899887766",
            email = "malhotramedicos@yahoo.com",
            address = "22, Old Railway Road",
            city = "Delhi",
            state = "Delhi",
            gstin = "07CCCPK1029M1ZN",
            openingBalance = 4500.0
        ),
        Client(
            id = "cli_4",
            name = "Meenakshi Joshi",
            businessName = "Walk-in Retail Customer",
            phone = "9810023456",
            email = "",
            address = "House 104, Sector 23",
            city = "Gurugram",
            state = "Haryana",
            gstin = "",
            openingBalance = 0.0
        )
    )

    fun createInitialInvoices(): List<Invoice> {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        return listOf(
            Invoice(
                id = "inv_1",
                invoiceNumber = "INV-2026-001",
                date = now - (1 * dayMs),
                dueDate = now + (6 * dayMs),
                clientId = "cli_1",
                clientName = "Dr. Rajesh Sharma",
                clientPhone = "9823456781",
                clientAddress = "Shop 5, Guru Nanak Market, Civil Lines, Gurugram",
                clientGstin = "06ABFPS8291P1ZK",
                items = listOf(
                    InvoiceItem("item_1", "prod_1", "Chyawanprash Awaleha (Special)", "30049011", 5.0, "Jar (500g)", 395.0, 12.0),
                    InvoiceItem("item_2", "prod_2", "Ashwagandha Gold Capsules", "30049011", 4.0, "Bottle (60 Cap)", 540.0, 12.0)
                ),
                discountAmount = 100.0,
                shippingCharge = 0.0,
                gstType = GstType.INTRA_STATE,
                isGstInclusive = true,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = PaymentMode.UPI,
                amountPaid = 4035.0,
                notes = "Delivered via express courier."
            ),
            Invoice(
                id = "inv_2",
                invoiceNumber = "INV-2026-002",
                date = now,
                dueDate = now + (7 * dayMs),
                clientId = "cli_2",
                clientName = "Sunita Verma",
                clientPhone = "9871122334",
                clientAddress = "B-12, Galleria Market, DLF Phase 4",
                clientGstin = "06BCVPS1928K1ZL",
                items = listOf(
                    InvoiceItem("item_3", "prod_6", "Kumkumadi Face Glow Tailam", "33049910", 3.0, "Dropper (30ml)", 850.0, 18.0),
                    InvoiceItem("item_4", "prod_3", "Triphala Churna Pure", "30049011", 10.0, "Pouch (200g)", 145.0, 5.0)
                ),
                discountAmount = 0.0,
                shippingCharge = 50.0,
                gstType = GstType.INTRA_STATE,
                isGstInclusive = true,
                paymentStatus = PaymentStatus.PAID,
                paymentMode = PaymentMode.CASH,
                amountPaid = 4050.0,
                notes = "Cash received at store counter."
            ),
            Invoice(
                id = "inv_3",
                invoiceNumber = "INV-2026-003",
                date = now,
                dueDate = now + (5 * dayMs),
                clientId = "cli_3",
                clientName = "Vikas Malhotra",
                clientPhone = "9899887766",
                clientAddress = "22, Old Railway Road, Delhi",
                clientGstin = "07CCCPK1029M1ZN",
                items = listOf(
                    InvoiceItem("item_5", "prod_5", "Brahmi Vati with Gold", "30049011", 6.0, "Strip (30 Tab)", 680.0, 12.0),
                    InvoiceItem("item_6", "prod_4", "Mahanarayan Taila (Joint Relief)", "30049011", 8.0, "Bottle (100ml)", 280.0, 12.0)
                ),
                discountAmount = 250.0,
                shippingCharge = 100.0,
                gstType = GstType.INTER_STATE,
                isGstInclusive = true,
                paymentStatus = PaymentStatus.UNPAID,
                paymentMode = PaymentMode.BANK_TRANSFER,
                amountPaid = 0.0,
                notes = "Interstate dispatch to Delhi warehouse."
            )
        )
    }

    val initialExpenses = listOf(
        Expense("exp_1", "Shop Electricity Bill", "Utilities", 3450.0, System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L), PaymentMode.UPI, "DHBVN-OCT-092"),
        Expense("exp_2", "Packing Boxes & Bubble Wrap", "Packaging", 1800.0, System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L), PaymentMode.CASH, "Inv-839"),
        Expense("exp_3", "Store Assistant Daily Wage", "Salary", 1200.0, System.currentTimeMillis(), PaymentMode.CASH, "Weekly wage"),
        Expense("exp_4", "Herbal Raw Material Delivery", "Logistics", 950.0, System.currentTimeMillis(), PaymentMode.UPI, "Porter booking")
    )
}
