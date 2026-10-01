package com.caderninho.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class SaleItemEntityTest {

    @Test
    fun `subtotal rounds quantity times unit cents`() {
        val item = SaleItemEntity(
            saleId = 1,
            description = "Produto por peso",
            quantity = 0.333,
            unitValueCents = 1_000,
            position = 0
        )

        assertEquals(333L, item.subtotalCents)
    }
}
