package com.example

import com.example.data.ErpRepository
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ErpRepositoryTest {
    private lateinit var repository: ErpRepository

    @Before
    fun setUp() {
        repository = ErpRepository()
    }

    @Test
    fun testInitialProductsLoaded() {
        val products = repository.products.value
        assertTrue("Products should not be empty", products.isNotEmpty())
        assertTrue("Should contain Chyawanprash", products.any { it.name.contains("Chyawanprash") })
    }

    @Test
    fun testStockAdjustmentAndInvoiceDeduction() {
        val prod = repository.products.value.first()
        val initialStock = prod.stock
        repository.adjustStock(prod.id, 5)
        assertEquals(initialStock + 5, repository.products.value.first { it.id == prod.id }.stock)

        // Adding an invoice should deduct inventory
        val invoice = Invoice(
            id = "test_inv",
            invoiceNumber = "INV-TEST-001",
            date = System.currentTimeMillis(),
            clientId = "cli_1",
            clientName = "Test Client",
            items = listOf(
                InvoiceItem(
                    id = "item_test",
                    productId = prod.id,
                    name = prod.name,
                    quantity = 3.0,
                    rate = prod.price
                )
            )
        )
        repository.addInvoice(invoice)
        assertEquals(initialStock + 2, repository.products.value.first { it.id == prod.id }.stock)
    }

    @Test
    fun testGstCalculationInclusiveAndExclusive() {
        // Item with ₹112 rate, 12% GST
        val item = InvoiceItem(
            id = "item_1",
            name = "Test",
            quantity = 1.0,
            rate = 112.0,
            gstRate = 12.0
        )

        // Inclusive: Taxable should be 100, Tax should be 12
        val (taxableInc, taxInc) = item.calculateTax(isInclusive = true)
        assertEquals(100.0, taxableInc, 0.01)
        assertEquals(12.0, taxInc, 0.01)

        // Exclusive: Taxable should be 112, Tax should be 112 * 0.12 = 13.44
        val (taxableExc, taxExc) = item.calculateTax(isInclusive = false)
        assertEquals(112.0, taxableExc, 0.01)
        assertEquals(13.44, taxExc, 0.01)
    }

    @Test
    fun testVoiceQueryProcessing() {
        val hindiSalesQuery = repository.processVoiceQuery("आज की सेल कितनी है")
        assertEquals("Today's Collection", hindiSalesQuery.title)
        assertTrue(hindiSalesQuery.response.contains("आज का कुल कलेक्शन"))

        val lowStockQuery = repository.processVoiceQuery("low stock products")
        assertEquals("Stock Alert / कम स्टॉक", lowStockQuery.title)

        val gstQuery = repository.processVoiceQuery("gst tax summary")
        assertEquals("GST Summary / जीएसटी रिपोर्ट", gstQuery.title)
    }
}
