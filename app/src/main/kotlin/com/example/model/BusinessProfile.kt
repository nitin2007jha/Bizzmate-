package com.example.model

data class BusinessProfile(
    val name: String = "Dev Ayurveda & Wellness",
    val tagline: String = "Pure Ayurvedic Products & Herbal Formulations",
    val phone: String = "+91 98765 43210",
    val email: String = "contact@devayurveda.in",
    val address: String = "Plot 42, Sector 18, Industrial Area",
    val city: String = "Gurugram, Haryana",
    val pincode: String = "122015",
    val gstin: String = "06AAAAA0000A1Z5",
    val upiId: String = "devayurveda@upi",
    val isGstInclusive: Boolean = true,
    val bankName: String = "State Bank of India",
    val accountNumber: String = "392019283719",
    val ifscCode: String = "SBIN0001234",
    val invoiceTerms: String = "1. Goods once sold will not be taken back.\n2. Interest @ 18% p.a. will be charged after due date.\n3. Subject to local jurisdiction."
)
