package com.caderninho.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class VendaItemEntityTest {

    @Test
    fun `subtotal rounds quantity times unit cents`() {
        val item = VendaItemEntity(
            vendaId = 1,
            descricao = "Produto por peso",
            quantidade = 0.333,
            valorUnitarioCentavos = 1_000,
            ordem = 0
        )

        assertEquals(333L, item.subtotalCentavos)
    }
}
